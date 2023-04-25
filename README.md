# learning Slick (the functional orm)

## Purpose
This project is a proof of concept work to encapsulate database access using slick.

The output of this project is a library that can be consumed by various other projects with minimal exposure of the underlying database technology.

The library exposes an api consisting of data types, actions and possible exceptions.  It also provides an implementation of this api using slick.   Eventually the api could be refactored into a separate library and a slick implementation.  This would allow for different implementations in the future.

## Goals
 - investigate how slick works
 - investigate possible code organization patterns
 - investigate test approaches
 - learn more about integration with Postgres

## Database
This project uses a database hosted on a postgres server.  

Instructions on how to setup a local docker instance of postgres are found [here](localPostgres.md)

Since the purpose of the project is to investigate slick and general best practices rather than database design or SQL best practices, no effort was made in database design and the sample database was copied from the book *The Practical SQL Handbook* by by Judith S. Bowman, Marcy Darnovsky, Sandra L. Emerson, ISBN: 0201703092.
The database is called book_biz and represents a fictitious publishing company.  


### Environment
The following environment variables are required:
- *BZ_USER* -- the user name for access to the book_biz database
- *BZ_PASSWORD* -- the password for access to the book_biz database
- *BZ_SCHEMA* -- the schema in the book_biz database

this schema is typically:
- *local* for individual testing
- *dev* for the shared development environment
- *qa* for separate qa testing
- *prod* for production deployments

## Release Steps
This project is based on the git-flow pattern described by [Atlassia](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow)

Specific steps are detailed [here](release.md)