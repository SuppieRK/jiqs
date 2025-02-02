# JIQS

JIQS (pronounced as "jicks," rhyming with "kicks") repository offers a template for the alternative highly customizable
Java microservice stack with focus on simplicity, modularity and control over dependencies where each piece can be
determined individually and excluded / replaced.

It should also open a path for [platform engineers](https://platformengineering.org/blog/what-is-platform-engineering)
to craft a bigger abstraction, tailored for specific company business needs to reduce friction during development.

## What contributes to the simplicity?

- Focusing on Java language, less focus on the annotations / frameworks to stay true
  to [YAGNI](https://en.wikipedia.org/wiki/You_aren%27t_gonna_need_it) principle.
- Focusing on pure SQL, no
  ORM [for](https://martinfowler.com/bliki/OrmHate.html) [several](https://medium.com/building-the-system/dont-be-a-sucker-and-stop-using-orms-190add65add4) [reasons](https://en.wikipedia.org/wiki/Object%E2%80%93relational_impedance_mismatch).

## Dependencies offered by default

- [Javalin](https://javalin.io/) as a web server core.
- [jOOQ](https://www.jooq.org/) as a way to talk to the database in pure SQL.
- [Flyway](https://www.red-gate.com/products/flyway/) as a database migration tool which also leverages SQL.
- [Gestalt](https://gestalt-config.github.io/gestalt/) as a customizable configuration library.
- [Inject](https://github.com/SuppieRK/inject) as a standalone dependency injection library.
- [Spotless](https://github.com/diffplug/spotless) to maintain code formatting rules.

### Glue dependencies

- [jooq-java-class-generator](https://github.com/SuppieRK/jooq-java-class-generator) to glue Flyway and jOOQ together.
- [Javalin Swagger](https://javalin.io/tutorials/openapi-example) to provide API documentation for consumers.
- [Google JIB](https://github.com/GoogleContainerTools/jib) to build deployable Docker image artifact.

### Other dependencies

`build.gradle` has dependencies split into sections, where `PREFERENTIAL` sections can and should be replaced to suit
your needs. 