# Starter MongoDB

The module `sda-commons-starter-mongodb` provides several autoconfigured features including:

  - The Read/Write`ZonedDateTime` converter
  - Automatic index creation

Based on:
  - `org.springframework.boot:spring-boot-starter-data-mongodb`

For further documentation please have a look at the Spring Data MongoDB [reference documentation](https://docs.spring.io/spring-data/mongodb/docs/current/reference/html/).

## Main Configuration

--8<-- "doc-snippets/config-starter-mongodb.md"

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

Testing services with MongoDB is [covered in the testing documentation](./web-testing.md#mongodb).
