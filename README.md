# sda-spring-boot-commons
[![Java CI](https://github.com/SDA-SE/sda-spring-boot-commons/actions/workflows/java-ci.yaml/badge.svg)](https://github.com/SDA-SE/sda-spring-boot-commons/actions/workflows/java-ci.yaml)
[![FOSSA Status](https://app.fossa.com/api/projects/custom%2B8463%2Fsda-spring-boot-commons.svg?type=shield)](https://app.fossa.com/reports/868957a2-81ed-4de3-8d43-dd59da3f8c68)

![spring](https://user-images.githubusercontent.com/61695677/155694976-dc7f9376-32ef-4be0-a919-3cc41a1f0341.png)

A set of libraries to bootstrap spring boot services easily that follow the patterns and
specifications promoted by the SDA SE.

## Features

| **Starter**                                          | **Description**                                                                                                                                                       |
|------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [sda-commons-starter-web](docs/web/index.md)         | Provides the required features for an SDA-compliant microservice including OIDC authentication, OPA authorization, health checks, OpenTracing and Prometheus metrics. |
| [sda-commons-starter-mongodb](docs/mongodb/index.md) | Provides default configuration based on the `org.springframework.boot:spring-boot-starter-data-mongodb`                                                               |
| [sda-commons-starter-kafka](docs/kafka/index.md)     | Provides default producer und consumer configuration based on `org.springframework.kafka:spring-kafka`                                                                |
| [sda-commons-starter-s3](docs/s3/index.md)           |                                                                                                                                                                       |


## Changelog and Versioning

This project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

See our [changelog](https://github.com/SDA-SE/sda-dropwizard-commons/releases/) for more information about the latest features.

## Getting Started

Have a look in our [documentation](docs/index.md)!