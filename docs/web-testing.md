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

Since version 7, the embedded MongoDB instance used for testing has been observed to exhibit
increased memory consumption, which may lead to slower test execution, higher resource usage, or
even out-of-memory errors in environments with limited resources.

This is typically due to each test case starting its own isolated MongoDB instance, which increases
memory overhead and degrades performance.

To mitigate this issue, it is possible to use a shared embedded MongoDB instance that runs once for
all integration tests in your suite. This approach significantly reduces memory overhead and
improves test execution speed.

??? example "Shared Embedded MongoDB for Integration Tests"
    ```java
    import de.flapdoodle.embed.mongo.config.Net;
    import de.flapdoodle.embed.mongo.distribution.Version;
    import de.flapdoodle.embed.mongo.transitions.Mongod;
    import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
    import de.flapdoodle.reverse.TransitionWalker;
    import de.flapdoodle.reverse.transitions.Start;
    
    import java.util.concurrent.atomic.AtomicBoolean;
    
    import org.junit.jupiter.api.extension.BeforeAllCallback;
    import org.junit.jupiter.api.extension.ExtensionContext;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    
    public class SharedMongoExtension implements BeforeAllCallback {
    
      private static final Logger LOGGER = LoggerFactory.getLogger(SharedMongoExtension.class);
      private static TransitionWalker.ReachedState<RunningMongodProcess> running;
      private static final AtomicBoolean started = new AtomicBoolean(false);
      private static final Object mutex = new Object();
    
      public static void stop() {
        if (running != null) {
          running.close();
        }
      }
    
      @Override
      public void beforeAll(ExtensionContext context) {
        synchronized (mutex) {
          try {
            if (started.compareAndSet(false, true)) {
              int port = de.flapdoodle.net.Net.freeServerPort();
              Mongod mongod =
                  Mongod.builder()
                      .net(Start.to(Net.class).initializedWith(Net.of("127.0.0.1", port, false)))
                      .build();
              running = mongod.start(Version.Main.V8_0);
              LOGGER.info("Started embedded MongodDB on Port {}", port);
              System.setProperty("spring.mongodb.uri", "mongodb://127.0.0.1:" + port + "/testdb");
            }
          } catch (Exception e) {
            throw new RuntimeException("Could not start embedded MongoDB", e);
          }
        }
      }
    }
    ```

To use this extension it is necessary to register it in the test class with:

```java

@ExtendWith(SharedMongoExtension.class)
public class MyIntegrationTest {
  // Your tests will now share the same MongoDB instance
}
```

To prevent Spring from starting its own embedded MongoDB instance, add the following to the
application.properties or as a JVM argument:

```properties
spring.autoconfigure.exclude=de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration
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

Since version 7, performance issues with the embedded Kafka for testing have been observed. This is typically due to each test case starting its own isolated Kafka instance, which increases memory overhead and degrades performance.

To mitigate this issue, it is possible to configure a shared embedded Kafka broker that runs once for the entire test suite. This approach significantly reduces memory overhead and improves test execution speed.

To achieve this:

1. Remove the @EmbeddedKafka annotation from the test classes if necessary, as it will now be replaced by Gradle configuration.
2. Configure the embedded Kafka broker via build.gradle, using system properties to control its behavior.

Update the test block in the build.gradle with the following system properties to enable a shared embedded Kafka broker:

```groovy
test {
    systemProperty("spring.kafka.global.embedded.enabled", "true")
    systemProperty("spring.kafka.embedded.ports", "0") // Use a single random port
    systemProperty("spring.kafka.embedded.partitions", "1")
}
```
While this setup improves performance, direct access to the EmbeddedKafkaBroker class is lost, which provides useful features like:

- Direct control over broker behavior
- Debugging capabilities (e.g., inspecting logs, topics)
- The ability to reset partitions or clear data between tests

This means that advanced configuration and debugging must be managed outside the EmbeddedKafkaBroker class, which can pose a limitation for complex test scenarios.

## S3

`sda-commons-web-testing` provides the annotation `S3Test` to start a local S3 mock with
[Robothy local-s3](https://github.com/Robothy/local-s3) and configure Spring Boot as needed for
`sda-commons-starter-s3`.
`@S3Test` must be placed before `@SpringBootTest` if used for a full integration test with an
application context.
The dependency `io.github.robothy:local-s3-rest` must be added as test dependency to the project.
`software.amazon.awssdk:s3` is needed as well and comes with `io.awspring.cloud:spring-cloud-aws-s3`
via `sda-commons-starter-s3`.
The versions are managed by the library as described [above](#dependencies).

??? example "Test S3 Clients in Spring Boot Application"
    ```java
    --8<-- "sda-commons-app-example/src/test/java/org/sdase/commons/spring/boot/web/app/example/S3FileRepositoryIntegrationTest.java:12"
    ```

!!! info "Test S3 Clients without Spring Context"
    `S3Test` can also be used in tests without a Spring Context.
    Test, set up and tear down methods can request `S3Client` and `LocalS3Configuration` as
    method parameter to interact with the S3 mock or set up the tested services.
