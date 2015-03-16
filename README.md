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

### GET /mydetic/api/v1.0/memories

Returns a list of dates that contain memories for a `uid`. It is not possible to query more than one `uid`
at a time.

*  **URL Params**

   **Required:**
 
   `uid=[alphanumeric]`

   **Optional:**
 
   `start_date=[YYYY-MM-DD]`
   `end_date=[YYYY-MM-DD]`
   
   Optionally restrict returned dates to those later than or equal to `start_date` and/or earlier than or
   equal to `end_date`. You can specify either, none or both of `start_date` and `end_date`.
   

* **Success Response:**
  
  * **Code:** 200 <br />
    **Content:** application/json
    ```json
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

