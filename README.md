# Bellman

Bellman executes SparQL queries in Spark.

## Modules

### Algebra parser

A parser for converting Sparql queries to Algebraic data types. These
ADTs can later be used to generate queries for the target system.

### Spark engine

The Spark engine runs after the algebra parser, and produces Spark
jobs that execute your SparQL queries.

## Publishing

In order to publish a new version of the project one must create a new
release in Github.  The release version must be of the format `v*.*.*`.
