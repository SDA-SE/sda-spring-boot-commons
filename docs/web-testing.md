# Testing

The module [`sda-commons-web-testing`](https://central.sonatype.com/artifact/org.sdase.commons.spring.boot/sda-commons-web-testing)
provides a few helpers to support integration tests related to the features provided by SDA Spring
Boot Commons and propagated patterns of SDA SE.

## Test Setup


### Dependencies

```groovy
dependencies {
  implementation enforcedPlatform("org.sdase.commons.spring.boot:sda-commons-dependencies:$sdaSpringCommonsVersion")
  implementation enforcedPlatform("org.sdase.commons.spring.boot:sda-commons-bom:$sdaSpringCommonsVersion")

  implementation 'org.sdase.commons.spring.boot:sda-commons-starter-web'
  
  testImplementation 'org.sdase.commons.spring.boot:sda-commons-web-testing'
}
```


### Management Port

The default configuration of Spring's Actuator with this library is a separate management port.
In tests, this causes some issues that
[need configuration](https://github.com/spring-projects/spring-boot/issues/4424#issuecomment-420276806).

`@SpringBootTest`'s need to be marked with `@DirtiesContext` or define `management.server.port=0`
either in `@SpringBootTest(properties = "…")` or in `src/test/resources/application.properties`.


### Provided Libraries

`sda-commons-web-test` comes with managed and aligned transitive dependencies, that can be used
without adding them to the dependency tree, including:

- `org.springframework.boot:spring-boot-starter-test`
- `org.springframework.cloud:spring-cloud-contract-wiremock`
- `org.junit.jupiter:junit-jupiter`
- `org.assertj:assertj-core`
- `com.jayway.jsonpath:json-path`
- `org.awaitility:awaitility`
- `org.mockito:mockito-junit-jupiter`


## Authentication and Authorization

The security concept of SDA SE has a strict separation of [authentication](./starter-web.md#authentication)
and [authorization](./starter-web.md#authorization).
The identity of a user is verified in the application by validating a JWT from an OIDC provider.
The authorization is delegated to an Open Policy Agent sidecar with policies for an actual
environment.
The service implementation grants access based on constraints received from the policy, not on roles
which may be different in each environment or not even available.

`sda-commons-web-testing` provides `ApplicationContextInitializer`s for integration test contexts to
work with authenticated users and mocked authorization decisions, including constraints as well as
for disabling the security mechanism.

??? example "Mocking Authentication and Authorization Constraints"
    ```java
    --8<-- "sda-commons-app-example/src/test/java/org/sdase/commons/spring/boot/web/app/example/AuthTest.java:12"
    ```

??? example "Disable Authentication and Authorization"
    ```java
    --8<-- "sda-commons-app-example/src/test/java/org/sdase/commons/spring/boot/web/app/example/DisabledAuthTest.java:12"
    ```

Usually constraints are handled in `@Controllers` who call the allowed business functions with
applicable parts of the constraints model.
Integration tests with HTTP calls may not be the best solution to test complex logic on constraints.
In such cases the constraints model can be mocked.

??? example "Mocking Constraints for Unit Tests Compared to Integration Test"
    === "SomeControllerTest"
        ```java
        --8<-- "sda-commons-app-example/src/test/java/org/sdase/commons/spring/boot/web/app/constraints/SomeControllerTest.java:12"
        ``` 
    === "SomeControllerIntegrationTest"
        ```java
        --8<-- "sda-commons-app-example/src/test/java/org/sdase/commons/spring/boot/web/app/constraints/SomeControllerIntegrationTest.java:12"
        ``` 
    === "SomeConstraints"
        ```java
        --8<-- "sda-commons-app-example/src/test/java/org/sdase/commons/spring/boot/web/app/constraints/test/SomeConstraints.java:12"
        ``` 
    === "SomeController"
        ```java
        --8<-- "sda-commons-app-example/src/test/java/org/sdase/commons/spring/boot/web/app/constraints/test/SomeController.java:12"
        ``` 


## Generating and Validating up-to-date Documentation

At SDA SE it is common to publish generated documentation like OpenAPI or AsyncAPI in the repository.
From there it's picked up by our developer portal based on Backstage.

`sda-commons-web-testing` provides `GoldenFileAssertions` to validate in a test that such
documentation is up-to-date and updates it when run locally.

??? example "Generating and updating OpenAPI"
    ```java
    --8<-- "sda-commons-app-example/src/test/java/org/sdase/commons/spring/boot/web/app/example/OpenApiDocumentationTest.java:12"
    ```


## MongoDB

It is recommended to test with an embedded MongoDB using Flapdoodle's Spring Boot module
`de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring3x`and the SDA Spring Boot Commons testing
module.
The version is managed by the library as described [above](#dependencies).

Flapdoodle will start a MongoDB server and configures the connection for Spring Boot tests.
The MongoDB version can be selected with (test) application properties:

```properties
de.flapdoodle.mongodb.embedded.version=4.4.1
```

To skip Flapdoodle's autoconfiguration and use a provided database (e.g. an AWS DocumentDB), the
property `test.mongodb.connection.string` (or environment variable `TEST_MONGODB_CONNECTION_STRING`)
can be used to provide a complete [MongoDB Connection String](https://docs.mongodb.com/manual/reference/connection-string/).
This feature is provided by the SDA Spring Boot Commons testing module and is only activated when
Flapdoodle's autoconfiguration is available for the test setup.

Clean up of the collections is important, especially when using a provided database, that may be
reused in a subsequent build.

To ensure backwards compatibility of the database after upgrades, refactorings or changing the
database framework, asserting with a bare `MongoClient` removes influence of mappers and converters.

??? example "Testing a MongoDB Repository"
    ```java
    --8<-- "sda-commons-starter-mongodb/src/test/java/org/sdase/commons/spring/boot/mongodb/TestEntityRepositoryTest.java:12"
    ```


## Kafka

It is recommended to test with an embedded Kafka using the Spring Boot module
`org.springframework.kafka:spring-kafka-test` and the SDA Spring Boot Commons testing module.
The version is managed by the library as described [above](#dependencies).

??? example "Integration Test for Kafka Consumer"
    ```java
    --8<-- "sda-commons-starter-kafka/src/test/java/org/sdase/commons/spring/boot/kafka/KafkaConsumerIntegrationTest.java:12"
    ```

??? example "Integration Test for Kafka Producer"
    ```java
    --8<-- "sda-commons-starter-kafka/src/test/java/org/sdase/commons/spring/boot/kafka/KafkaProducerIntegrationTest.java:12"
    ```
