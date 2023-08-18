# sda-spring-boot-commons
[![Latest Release](https://img.shields.io/github/v/release/sda-se/sda-spring-boot-commons?label=latest)](https://github.com/SDA-SE/sda-spring-boot-commons/releases/latest)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.sdase.commons.spring.boot/sda-commons-starter-web/badge.svg)](https://search.maven.org/search?q=org.sdase.commons.spring.boot)
[![Java CI](https://github.com/SDA-SE/sda-spring-boot-commons/actions/workflows/java-ci.yaml/badge.svg)](https://github.com/SDA-SE/sda-spring-boot-commons/actions/workflows/java-ci.yaml)
[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B8463%2Fsda-spring-boot-commons.svg?type=shield)](https://app.fossa.com/reports/868957a2-81ed-4de3-8d43-dd59da3f8c68)

![spring](https://user-images.githubusercontent.com/61695677/155694976-dc7f9376-32ef-4be0-a919-3cc41a1f0341.png)

A set of libraries to bootstrap spring boot services easily that follow the patterns and
specifications promoted by the SDA SE.

> :partying_face: Upgrade to Spring Boot 3 is released with [version 3.0.0](https://github.com/SDA-SE/sda-spring-boot-commons/releases/tag/3.0.0). :partying_face:
> 
> Please follow [the migration guide](./docs/migration-2-to-3.md) to upgrade.

## Features

| **Starter**                                            | **Description**                                                                                                                                                         |
|--------------------------------------------------------|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [sda-commons-starter-web](docs/starter-web.md)         | Provides the required features for an SDA-compliant microservice including OIDC authentication, OPA authorization, health checks, OpenTelemetry and Prometheus metrics. |
| [sda-commons-starter-mongodb](docs/starter-mongodb.md) | Provides default configuration based on the `org.springframework.boot:spring-boot-starter-data-mongodb`                                                                 |
| [sda-commons-starter-kafka](docs/starter-kafka.md)     | Provides default producer und consumer configuration based on `org.springframework.kafka:spring-kafka`                                                                  |
| [sda-commons-starter-s3](docs/s3.md)                   |                                                                                                                                                                         |


## Changelog and Versioning

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

See our [changelog](https://github.com/SDA-SE/sda-spring-boot-commons/releases/) for more information about the latest features.

## Getting Started

Have a look in our [documentation](docs/index.md)!