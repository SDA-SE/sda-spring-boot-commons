# Migration Guide for Upgrading from 2.x.x to 3.x.x

SDA Spring Boot Commons 3 updates 

- Spring Boot from 2.7.x to 3.1.x and 
- Spring Cloud from 2021.x.x to 2022.x.x.

This comes with some breaking changes introduced by Spring and some from SDA Spring Boot Commons as
well.
Other libraries are upgraded or replaced as well.

In addition to this migration guide, the [official migration guide of Spring Boot](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
should be consulted.

Summary of noticeable changes:

- `javax` dependencies are replaced by `jakarta` dependencies.
- Tracing moved from Sleuth to Open Telemetry.
- AsyncAPI setup has been changed, as well as the used library to generate Json Schemas from code.
  Using of the generator is simpler now as [described in its documentation](asyncapi.md).
- A major upgrade of Spring Security is included.
- Spring configuration properties changed.
- S3 libraries changed to a new and different Java API of AWS.


## Jakarta EE

Please make sure dependencies do not pull in transitive `javax` modules and migrate all `javax`
imports to `jakarta`.
The provided dependency management should take care about all dependencies by referring to the
dependency management of Spring Boot as mentioned in [the migration guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide#jakarta-ee).

Please note that Wiremock repackaged `javax` classes.
They will be available in tests but should not be used in your code.


## Tracing

Tracing uses OpenTelemetry now.
Any configuration that references `zipkin` or `sleuth` is not considered anymore.

Please refer to the respective `management.tracing` properties in
[the documentation](starter-web.md#configuration).


## AsyncAPI generation

Json Schemas for AsyncAPI are generated with
[Victools' Json Schema Generator](https://github.com/victools/jsonschema-generator) now.
The [previously used library](https://github.com/mbknor/mbknor-jackson-jsonSchema) is barely
maintained in the past years.

The old library provided their own annotations.
Now, annotations of Jackson (e.g. `@JsonSchemaDescription`), Swagger (e.g. `@Schema`) and Jakarta
Validation (e.g. `NotNull`) can be used.
Note that not all attributes of all annotations are covered and multiple examples are not possible
anymore.
Only one example can be defined with `@Schema(example = "value")`.

How the Java classes for schema definitions in the AsyncAPI are defined has changed.
Previously, classes to integrate were defined in the code
(`.withSchema("./schema.json", BaseEvent.class)`) and referenced in the AsyncAPI template
(`$ref: './schema.json#/definitions/CarManufactured'`).
Now the classes are referenced directly in the template (`$ref: 'class://com.example.BaseEvent`).
The builder method `withSchema` does not exist any more.

Please review the differences in the generated AsyncAPI file.
Both libraries work different and have a different feature set.
The new generator may have some limitations but a great API for extensions.
Please [file an issue](https://github.com/SDA-SE/sda-spring-boot-commons/issues) if something
important can't be expressed.


## MongoDB

If you use `de.flapdoodle.embed:de.flapdoodle.embed.mongo` for testing, change to
`de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x`.
To define the database version used for tests, use the property
`de.flapdoodle.mongodb.embedded.version` instead of `spring.mongodb.embedded.version`


## S3

TODO new library, new packages, new classes, testing? findify brings the old lib

