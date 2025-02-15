# Platform Library

Defines common structural components of the service.

Typically, this library should define the most generic and general components for any service to use within a company:

- Web server to handle HTTP requests.
- Configuration reader.
- Database connection pool.
- Message bus connection primitives.
- And many more.

> It is also a very good idea to create a separate project to maintain Bill of Materials which would define dependency
> versions for additional control.

## Dependencies offered by default

`build.gradle` in this project defines `api` dependencies split into sections, where seconds commented with
`PREFERENTIAL` can and should be replaced to suit your needs.

### Core

- [Javalin](https://javalin.io/) as a web server core:
    - Check out [TechEmpower benchmarks](https://www.techempower.com/benchmarks) for more
      options. [Jooby](https://jooby.io/) is a very viable replacement here.
- [jOOQ](https://www.jooq.org/) as a way to talk to the database in pure SQL.
- [Flyway](https://www.red-gate.com/products/flyway/) as a database migration tool which also leverages SQL.

### Optional

- [Inject](https://github.com/SuppieRK/inject) as a standalone dependency injection library:
    - This can be a nice Quality of Life addition, but with enough attention to details can be removed - after all any
      direct object creation will be much faster than any reflection-based Dependency Injector.
    - If you are adamant on the need for Dependency Injection, as alternative I would personally recommend checking out
      a set of libraries from [Avaje](https://avaje.io/inject/).
- [Gestalt](https://gestalt-config.github.io/gestalt/) as a customizable configuration library:
    - Alternatively you can directly read and parse configuration file using Jackson / SnakeYaml to reduce clutter.

### Code health

- [Spotless](https://github.com/diffplug/spotless) to maintain code formatting rules.