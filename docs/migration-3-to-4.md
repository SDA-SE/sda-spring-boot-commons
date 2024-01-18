# Migration Guide for Upgrading from 3.x.x to 4.x.x

SDA Spring Boot Commons 4 updates 

- Spring Boot from 3.1.x to 3.2.x and 
- Spring Cloud from 2022.x.x to 2023.x.x.

This comes with some breaking changes introduced by Spring and some from SDA Spring Boot Commons as
well.
Other libraries are upgraded or replaced as well.

In addition to this migration guide, the [official release notes of Spring Boot](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.2-Release-Notes)
should be consulted.

Summary of noticeable changes:

- Migrate to AWS SDK v2
- Replace S3Mock with [Robothy's local-s3](https://github.com/Robothy/local-s3) JUnit 5 extension
- Springs `RestClient` and `RestTemplate` no longer buffer request bodies by default. As a result,
  for certain content types such as JSON, the contents size is no longer known and a Content-Length
  header is no longer set
- Http request body size is no longer limited by the server by default and can no longer be
  controlled by `request.body.max.size`. Control over the request body size should be delegated to
  the underlying infrastructure