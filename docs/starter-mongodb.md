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
* `spring.mongodb.uri` _string_
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

## Migrating UUID representation

Version 7 updated Spring Boot Commons to version 4. As a result:
> Spring Data MongoDB no longer provide defaults for UUID and BigInteger/BigDecimal representations.
> This aligns with the driver recommendation to not favor a particular representation for UUID or
> BigInteger/BigDecimal to avoid representation changes caused by upgrades to a newer Spring Data
> version.

Applications now need to explicitly choose the representation they want to use. If no property was
previously set, the existing UUID representation is most likely `java_legacy`.

To help migrate existing UUID fields to the `standard` representation, you can set the environment
variable or property:

```
SPRING_MONGO_UUID_MIGRATE=true
```

Once enabled, Spring will automatically scan all MongoDB collections on application startup and
convert UUID fields from `java_legacy` to `standard` representation. This ensures consistency with
the newer driver behavior and avoids serialization or compatibility issues.

**Note:** It is recommended to back up your database before enabling migration, and test the
migration in a staging environment to verify data integrity.