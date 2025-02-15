# Platform Gradle Plugin

Defines common plugins and dependencies for services.

It is a good idea to package this plugin with:

- Platform library.
- Additional glue and settings for the plugins provided.

## Dependencies offered by default

### Core

- [Google JIB](https://github.com/GoogleContainerTools/jib) to build deployable Docker image artifact.
- [jooq-java-class-generator](https://github.com/SuppieRK/jooq-java-class-generator) to glue Flyway and jOOQ together.
    - There are other alternatives to this plugin as well!
