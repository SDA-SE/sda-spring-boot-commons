# SDA Spring Boot Commons

A set of libraries to bootstrap Spring Boot services easily that follow the patterns and
specifications promoted by the SDA SE.

!!! info "Spring Boot 3"
    🥳 Upgrade to Spring Boot 3 is released with
    [version 3.0.0](https://github.com/SDA-SE/sda-spring-boot-commons/releases/tag/3.0.0). 🥳
    
    Please follow [the migration guide](migration-2-to-3.md) to upgrade.


## Features

| **Starter**                                       | **Description**                                                                                                                                                                                               |
|---------------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| [sda-commons-starter-web](starter-web.md)         | Provides the required features for an SDA-compliant microservice including OIDC authentication, OPA authorization, health checks, OpenTelemetry, Prometheus metrics and [hardening the service](security.md). |
| [sda-commons-starter-mongodb](starter-mongodb.md) | Provides default configuration based on the `org.springframework.boot:spring-boot-starter-data-mongodb`.                                                                                                      |
| [sda-commons-starter-kafka](starter-kafka.md)     | Provides default producer und consumer configuration based on `org.springframework.kafka:spring-kafka`.                                                                                                       |
| [sda-commons-starter-s3](starter-s3.md)           | Provides features for dealing with the Amazon S3 file storage.                                                                                                                                                |
| [sda-commons-asyncapi](asyncapi.md)               | Provides utilities to create AsyncAPI schemas, usually used as test dependency to build the schema in a test.                                                                                                 |

The provided documentation aims to provide SDA-specific information.
All other information are referenced in the Spring and [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#documentation).

## Getting started

Each starter is provided as an isolated library and relies on
the Spring Boot and Spring Cloud dependency management. Most of the starters themselves include some
starters provided by Spring or the community.

When using any sda-spring-boot-commons starter make sure to include the provided dependency
management.
The provided dependency management is based on the Spring Boot and Spring Cloud dependency
management to align transitive dependencies with the provided Spring versions.

```groovy
project.ext {
  sdaSpringCommonsVersions = '0.11.0'
}
dependencies {
  implementation enforcedPlatform("org.sdase.commons.spring.boot:sda-commons-dependencies:$sdaSpringCommonsVersions")
  implementation enforcedPlatform("org.sdase.commons.spring.boot:sda-commons-bom:$sdaSpringCommonsVersions")

  implementation 'org.sdase.commons.spring.boot:sda-commons-starter-web'

  testImplementation "org.sdase.commons.spring.boot:sda-commons-web-testing"
}
```

With gradle 9 it will be mandatory to define a launcher to run your tests with gradle. SDA Spring Boot Commons uses junit. That's why it is recommended adding the following dependency to your defined dependencies:
```
testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
```

Artifacts of SDA Spring Boot Commons are available [at Maven Central](https://search.maven.org/search?q=g:org.sdase.commons.spring.boot)
since release [0.11.2](https://github.com/SDA-SE/sda-spring-boot-commons/releases/tag/0.11.2).

### Static directories
Since Spring Boot runs in an embedded Tomcat server, it needs some tmp directories to support the container run in a readonly file system.
Therefore, your application need to set a folder called `static` and a folder structure `tmp/tomcat` in the root directory of your container.
In order to do so, create a folder `static` and `tmp/tomcat` on `src/main/jib`.
If you use non-root docker images, your jib config in your `build.gradle` file must include `container.workingDirectory='/'`,
so jib will use the root folder to create the sub folders, e.g:

```gradle
jib {
  from.image = 'gcr.io/distroless/java17-debian12:nonroot'
  container.workingDirectory='/'
}
```

When the container's image is generated with `gradlew jibDockerBuild`, these folders will be copied to the container.
In case you need the tmp folder to be writable, you can mount a volume in your container. The default path is `/tmp/tomcat`, but you can overwrite it setting the environment variable pointing to your folder:

```
SERVER_TOMCAT_BASEDIR=/path-to-your-folder
```
