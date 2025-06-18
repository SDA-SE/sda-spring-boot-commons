# Migration Guide for Upgrading from 4.x.x to 5.x.x

SDA Spring Boot Commons 5 updates 

- Spring Boot from 3.4.x to 3.5.x and
- Spring Cloud from 2024.x.x to 2025.x.x.

This comes with some breaking changes introduced by Spring and some possible ones from SDA Spring Boot Commons as
well.
Other libraries are upgraded or replaced as well.

In addition to this migration guide, the [official release notes of Spring Boot](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.5-Release-Notes)
and [Spring Cloud](https://github.com/spring-cloud/spring-cloud-release/wiki/Spring-Cloud-2025.0-Release-Notes) should be consulted.

Summary of noticeable changes:

- It can happen that a misalignment occurs between the version of the junit-platform-launcher found by gradle and the version of the junit-jupiter-engine provided in the spring dependencies. In this case, gradle is not able to run the tests and throws an error. It's necessary to add testRuntimeOnly 'org.junit.platform:junit-platform-launcher' as a dependency in your build.gradle file then. That takes the version for the junit-platform-launcher from the dependency management. In general, it should be considered to add this dependency in your build.gradle file. With gradle 9 this dependency will be [mandatory](github.com/gradle/gradle/issues/26114).
- The provided and deprecated feign.jaxrs.JakartaContract got removed from the sda-commons-starter-web package.
- The provided and deprecated kafkaDltTemplate Bean got removed from the sda-commons-starter-kafka package.
- The Spring TestRestTemplate now uses the same redirect rules as the regular RestTemplate. Additionally, the HttpOption.ENABLE_REDIRECTS option has been deprecated.