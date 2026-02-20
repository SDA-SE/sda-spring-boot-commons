# Migration Guide for Upgrading from 6.x.x to 7.x.x

SDA Spring Boot Commons 7 updates 

- Spring Boot from 3.5.x to 4.0.x and
- Spring Cloud from 2025.0.x to 2025.1.x.
- Jackson 2.x.x to 3.x.x

This upgrade contains breaking changes originating from Spring, SDA Spring Boot Commons, and the transition to Jackson 3.
It also drops the support of Java 17 and adds the support of Java 25.

In addition to this migration guide, the [official migration guide of Spring Boot](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide)
and [official migration guide of Jackson](https://github.com/FasterXML/jackson/blob/main/jackson3/MIGRATING_TO_JACKSON_3.md) should be consulted.

## Summary of noticeable changes

* **Spring imports / package changes (Spring Boot 4 / Spring Framework 7)**

  * Spring has restructured and split several packages.
  * Existing imports may no longer resolve and need to be updated.
  * Refer to the official Spring Boot 4 migration guide for the complete package mapping table.

* **Jackson 2 → Jackson 3**

  * Jackson was upgraded from **2.x to 3.x**.
  * Dependency coordinates and Java packages changed from
    `com.fasterxml.jackson…` → `tools.jackson…` (exception Jackson annotations).
  * Code that directly references Jackson types (e.g. `ObjectMapper`, `JsonNode`, custom serializers/deserializers) must be updated accordingly.

* **Configuration property renames (Spring Boot 4) Examples**

Please refer to the guide for more information.
This property changes can make changes in consuming services, like deployments necessary.

| Old property                          | New property                                               |
|---------------------------------------|------------------------------------------------------------|
| `spring.data.mongodb.uri`             | `spring.mongodb.uri`                                       |
| `management.otlp.tracing.endpoint`    | `management.opentelemetry.tracing.export.otlp.endpoint`    |
| `management.otlp.tracing.compression` | `management.opentelemetry.tracing.export.otlp.compression` |
| `management.tracing.enabled`          | `management.tracing.export.enabled`                        |
| `management.otlp.tracing.timeout`     | `management.opentelemetry.tracing.export.otlp.timeout`     |

* Old property names are ignored and must be updated to take effect.

* **Test configuration change**
  * **WireMock**
    * `@AutoConfigureWireMock` was **removed**.
    * Use `@EnableWireMock` instead.
  * **TestRestTemplate**
    * To use **TestRestTemplate** in tests it is now necessary to use `@AutoConfigureTestRestTemplate` on your test class.
  * **Tracing**
    * `@AutoConfigureObservability` was split into `@AutoConfigureMetrics` and `@AutoConfigureTracing`
  * **Kafka**
    * If many tests utilize the `@EmbeddedKafka` annotation, memory consumption may degrade after
      the update. To mitigate this, consider using a single global embedded Kafka instance (as
      outlined in
      the [Spring Kafka documentation](https://docs.spring.io/spring-kafka/reference/testing.html#same-broker-multiple-tests)).
      This approach reduces resource overhead by sharing a single broker across tests. However, it
      comes with a caveat: you lose direct access to the `EmbeddedKafkaBroker` class, which may
      limit advanced configuration or debugging capabilities.
    * `listeners=PLAINTEXT://localhost:0` configuration in `@EmbeddedKafka` is not necessary any
      longer.
  * **MongoDB**
    * If many tests utilize the database, memory consumption may degrade after
      the update. To mitigate this, consider using a single global embedded MongoDB (as outlined
      in [web-testing](web-testing.md/#mongodb))

* Java 17 support removed.
* Java 25 support added. 
  * The distroless image for this java version is `gcr.io/distroless/java25-debian13`.