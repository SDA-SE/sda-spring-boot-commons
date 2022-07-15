# SDA Spring Boot Commons

A set of libraries to bootstrap spring boot services easily that follow the patterns and
specifications promoted by the SDA SE.

## Features

| **Starter**                                     | **Description**                                                                                                                                                       |
|-------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [sda-commons-starter-web](web/index.md)         | Provides the required features for an SDA-compliant microservice including OIDC authentication, OPA authorization, health checks, OpenTracing and Prometheus metrics. |
| [sda-commons-starter-mongodb](mongodb/index.md) | Provides default configuration based on the `org.springframework.boot:spring-boot-starter-data-mongodb`                                                               |
| [sda-commons-starter-kafka](kafka/index.md)     | Provides default producer und consumer configuration based on `org.springframework.kafka:spring-kafka`                                                                |
| [sda-commons-starter-s3](s3/index.md)           |                                                                                                                                                                       |

The provided documentation aims to provide SDA-specific information.
All other information are referenced in the Spring and [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#documentation).

## Getting started

Each starter is provided as an isolated library and relies on
the Spring Boot and Spring Cloud dependency management. Most of the starters themselves include some
starters provided by Spring or the community.

Since the library just want to provide SDA conventions and SDA specific implementations as bundled
starters, the Spring Boot dependency management is still used for providing the dependency versions.
So we will just provide a matrix of supported Spring Boot and Spring Cloud versions. But this may
change in the future.

When using any sda-spring-boot-commons starter make sure to enable the Spring dependency management
for e.g. `build.gradle` as following:

```groovy
plugins {
  id 'org.springframework.boot' version '2.7.1'
  id 'io.spring.dependency-management' version '1.0.11.RELEASE.RELEASE'
}

dependencies {
  implementation platform("org.springframework.cloud:spring-cloud-dependencies:2021.0.2")
  implementation "org.sdase.commons.spring.boot:sda-commons-starter-web:0.7.0"
}
```

**NOTE:** Currently the libraries are only available
via `https://nexus.sda-se.io/repository/sda-se-releases/`

## Supported Versions - Matrix

TODO We need to think about, if we really want to rely on that matrix or if we force a Spring Boot /
Cloud version with our library.

| **SDA Commons** | **Spring Boot** | **Spring Cloud**                              |
|-----------------|-----------------|-----------------------------------------------|
| < 0.7.0         | 2.7.1           | 2021.0.x aka Jubilee (starting with 2021.0.3) |

The Spring Boot / Spring Cloud compatiblity matrix is
provided [here](https://spring.io/projects/spring-cloud)