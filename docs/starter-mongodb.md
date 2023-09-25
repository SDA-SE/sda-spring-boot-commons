# Starter MongoDB

The module `sda-commons-starter-mongodb` provides several autoconfigured features including:

  - The Read/Write`ZonedDateTime` converter
  - Automatic index creation

Based on:
  - `org.springframework.boot:spring-boot-starter-data-mongodb`

For further documentation please have a look at the Spring Data MongoDB [reference documentation](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/).

## Main Configuration

| **Property**                                  | **Description**                                                                                                                 | **Default**                  | **Example**                      | **Env**                              |
|-----------------------------------------------|---------------------------------------------------------------------------------------------------------------------------------|------------------------------|----------------------------------|--------------------------------------|
| `spring.data.mongodb.uri` _string_            | The MongoDB [connection string](https://www.mongodb.com/docs/manual/reference/connection-string/#connection-string-uri-format). |                              | `mongodb://localhost:27017/test` | `SPRING_DATA_MONGODB_URI`            |
| `sda.caCertificates.certificatesDir` _string_ | A directory with CA certificates in PEM format that will be picked up to trust the connection to the database.                  | `"/var/trust/certificates"`  | `"/my-certs"`                    | `SDA_CACERTIFICATES_CERTIFICATESDIR` |


## Configuration properties
* `spring.data.mongodb.uri` _string_
  * Mongo database URI.
  * Example: `mongodb://exampleUser:examplePassword@mongoHost:27017`
  * Format: `mongodb://[username:password@]host1[:port1][,...hostN[:portN]][/[defaultauthdb][?options]]`
  * Connection String Options need to be added to the end of the URI e.g.
    * `?ssl=true` to enable SSL
    * `?retryWrites=false` to disable retryable writes for the connection.
    * `?readPreference=secondaryPreferred` In most situations, operations read from secondary
      members, but in situations where the set consists of a single primary (and no other members),
      the read operation will use the replica set's primary.
  * For further information take a look on
    [Connection String documentation](https://docs.mongodb.com/manual/reference/connection-string)

### SSL support

The mongodb starter can be configured to use ssl when the option `?ssl=true` is used.
Certificates in PEM format can be mounted in the directory `/var/trust/certificates` they will be
used by the mongodb client.
All certificates found in subdirectories will also be loaded.

Note that this directory is also configurable through the property `sda.caCertificates.certificatesDir`.


## Testing

It is recommended to test with an embedded MongoDB using Flapdoodle's Spring Boot module and the
SDA Spring Boot Commons testing module:

```groovy
dependencies {
  testImplementation 'de.flapdoodle.embed:de.flapdoodle.embed.mongo.spring30x'
  testImplementation 'org.sdase.commons.spring.boot:sda-commons-web-testing'
}
```

Flapdoodle will start a MongoDB server and configures the connection for Spring Boot tests.
The MongoDB version can be selected with (test) application properties:

```properties
de.flapdoodle.mongodb.embedded.version=4.4.1
```

`MongoOperations` can be used to clean the database between tests:

```java
  @Autowired MongoOperations mongoOperations;

  @BeforeEach
  void beforeEach() {
    mongoOperations.dropCollection("collectionUnderTest");
  }
```

To skip Flapdoodle's autoconfiguration and use a provided database (e.g. an AWS DocumentDB), the
property `test.mongodb.connection.string` (or environment variable `TEST_MONGODB_CONNECTION_STRING`)
can be used to provide a complete [MongoDB Connection String](https://docs.mongodb.com/manual/reference/connection-string/).
This feature is provided by the SDA Spring Boot Commons testing module and is only activated when
Flapdoodle's autoconfiguration is available for the test setup.
