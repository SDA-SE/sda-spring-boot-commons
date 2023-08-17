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
