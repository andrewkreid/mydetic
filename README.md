# MyDetic

> Eidetic: *relating to or denoting mental images having unusual vividness and detail, as if actually visible. "an eidetic memory"*

Yet Another [OhLife](http://ohlife.com/shutdown) Clone.

The intent is to make some code you can run yourself to store your own
diary entries rather than build a service that will scale to many users.

## Development Status

Pre-Alpha. Early development. Almost nothing works.

## Motivation

There was an online service called OhLife (now deceased) that was kind of an online diary
that let you enter a short description of what was going on in your life every day and stored
entries and made them searchable.

The magic was in the daily reminder, which would show you your memory from the same day from
some variation of a month/year/n years ago. I (at least) found this fun and compelling.

Alas, OhLife has shut down. There are a number of similar services (example: [Little Memory](https://thelittlememory.com)),
but I'm going to write another for the following reasons:

1. If I write and run the code myself, the service can't disappear on me.
2. After more than 20 years in IT, I have no public source code. I should have some.
3. It's a good excuse to write an Android app, which is something I want to do.

## Basic Architecture

* Store entries in Amazon S3 to start, but abstract the DAO for other data stores 
(thanks to [nMustaki](https://github.com/nMustaki) for contributing an SQLite store).
* Provide a REST API for manipulating the store, based on Flask-RESTful. (under construction)
* Write an Android app that uses the REST API. (not started)

## REST API

### General Error Response

API call errors will generally have the following format JSON in the response body:

```javascript
{
    "error_code": 103, 
    "long_message": "No memory available for mreynolds on 2015-12-11", 
    "short_message": "Invalid Data Operation"
}
```

Error codes are defined in ```errorcodes.py```. The current values are:

Error Code  | Description
------------|------------
100         | Invalid caller input (eg badly formatted date)
101         | Failed to read or write to/from the underlying data store. The long message should have more info
102         | Logically invalid data operation (eg deleting a memory that doesn't exist)
103         | Data in the data store is not in a recognised format.
999         | Unknown error.

### GET /mydetic/api/v1.0/memories

Returns a list of dates that contain memories for a `uid`. It is not possible to query more than one `uid`
at a time.

*  **URL Params**

   **Required:**

   `user_id=[alphanumeric]`

   **Optional:**

   `start_date=[YYYY-MM-DD]`
   `end_date=[YYYY-MM-DD]`
   
   Optionally restrict returned dates to those later than or equal to `start_date` and/or earlier than or
   equal to `end_date`. You can specify either, none or both of `start_date` and `end_date`.


* **Success Response:**

  * **Code:** 200 <br />
    **Content:** application/json

```javascript
{
    "memories": [
        "2015-12-18",
        "2015-12-19",
        "2015-12-20"
    ],
    "uid": "mreynolds"
}
```

* **Error Response:**

  * TODO

* **Sample Call:**

```
curl -v http://127.0.0.1:5000/mydetic/api/v1.0/memories?uid=mreynolds
```

### POST /mydetic/api/v1.0/memories

Creates a new memory

* **URL Params**

None

* **Request body**

JSON of the following form:

```javascript
{
    "memory_date": "2015-03-27", 
    "memory_text": "TGIF!", 
    "user_id": "mreynolds"
}
```

  * Dates must be in ISO format (YYYY-MM-DD)

* **Success Response:**

  * **Code:** 201 <br />
    **Content:** application/json

```javascript
{
    "created_at": "2015-03-28T05:10:12.609837", 
    "memory_date": "2015-03-27", 
    "memory_text": "TGIF!", 
    "modified_at": "2015-03-28T05:10:12.609837", 
    "user_id": "mreynolds"
}
```

  * The ```created_at``` and ```modified_at``` timestamps are always in UTC timezone and in ISO format.

* **Error Response:**

  * **Code:** 400 on invaliv input or if a memory on that date already exists. <br />
    **Content:** application/json

* **Sample Call:**

```
curl http://127.0.0.1:5000/mydetic/api/v1.0/memories \
  -X POST \
  -d '{"user_id": "mreynolds", "memory_date": "2015-03-27", "memory_text": "TGIF!"}' \
  --header "Content-Type: application/json"
```

### GET /mydetic/api/v1.0/memories/YYY-MM-DD

Returns a single memory for a particular date.

*  **URL Params**

   **Required:**

   `user_id=[alphanumeric]`

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** application/json

```javascript
{
    "created_at": "2015-11-13T05:14:13.548922",
    "memory_date": "2015-11-12",
    "memory_text": "Today my favourite TV show was cancelled :(",
    "modified_at": "2015-11-13T05:20:52.981991",
    "user_id": "mreynolds"
}
```

* **Error Response:**

  * **Code:** 404 <br />
    **Content:** application/json

* **Sample Call:**

```
curl -v http://127.0.0.1:5000/mydetic/api/v1.0/memories/2014-11-12?uid=mreynolds
```

### PUT /mydetic/api/v1.0/memories/YYY-MM-DD

Updates a single memory for a particular date.

*  **URL Params**

   **Required:**

   `user_id=[alphanumeric]`

* **Request body**

JSON of the following form:

```javascript
{
    "memory_date": "2015-03-27", 
    "memory_text": "TGIF!", 
    "user_id": "mreynolds"
}
```

**Note:** Only the memory_text field will be updated.

* **Success Response:**

  * **Code:** 200 <br />
    **Content:** application/json

Response body contains the updated memory JSON.

* **Error Response:**

  * **Code:** 404 (no memory on that date for the user_id)<br />
    **Content:** application/json
  * **Code:** 400 (invalid input)<br />
    **Content:** application/json


* **Sample Call:**

```
$ curl http://127.0.0.1:6000/mydetic/api/v1.0/memories/2015-03-21 \
    -X PUT \
    -d '{"user_id": "darren", "memory_date": "2015-03-21", "memory_text": "Updated memories..."}' \
    --header "Content-Type: application/json"
```

### DELETE /mydetic/api/v1.0/memories/YYY-MM-DD

Deletes a single memory on a particular date.

*  **URL Params**

   **Required:**

   `user_id=[alphanumeric]`

* **Request body**

   None
   
* **Success Response:**

  * **Code:** 200 (successfully deleted)<br />
    **Content:** application/json

Response body contains the deleted memory JSON.
```javascript
{
    "created_at": "2015-03-31T11:45:57.390296", 
    "memory_date": "2015-03-21", 
    "memory_text": "Deleted memories...", 
    "modified_at": "2015-04-04T06:23:06.352444", 
    "user_id": "mreynolds"
}
```

* **Error Response:**

  * **Code:** 404 (no memory on that date for the user_id)<br />
    **Content:** application/json
  * **Code:** 400 (invalid input)<br />
    **Content:** application/json


* **Sample Call:**

```
$ curl http://127.0.0.1:6000/mydetic/api/v1.0/memories/2015-03-21 \
    -X PUT \
    -d '{"user_id": "darren", "memory_date": "2015-03-21", "memory_text": "Updated memories..."}' \
    --header "Content-Type: application/json"
```
