# MyDetic

Yet Another [OhLife](http://ohlife.com/shutdown) Clone.

The intent is to make some code you can run yourself to store your own
diary entries rather than build a service that will scale to many users.

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

* Store entries in Amazon S3 to start, but abstract the DAO for other data stores (thanks to [nMustaki](https://github.com/nMustaki) for contributing an SQLite store).
* Provide a REST API for manipulating the store, based on Flask-RESTful. (under construction)
* Write an Android app that uses the REST API. (not started)


