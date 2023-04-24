# learning Slick (the functional orm)

## Purpose
This project is a proof of concept work to encapsulate database access using slick.

The output of the this project is a library that can be consumed by various other projects with minimal exposure of the underlying database technology.

The library exposes an api consisting of data types, actions and possible exceptions.  It also provides an implementation of this api using slick.   Eventually the api could be refactored into a separate library and a slick implementation.  This would allow for different implementations in the future.

## Goals
 - investigate how slick works
 - investigate possible code organization patterns
 - investigate test approaches
 - learn more about integration with Postgres

## Setup
This project requires access to a postgres server.
Instructions on how to setup a local docker instance of postgres are found [here](localPosgres.md)