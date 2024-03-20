# Starter Web

The module `sda-commons-starter-web` provides several features to create a service based on the SDA
core concepts.

###  Configuration

--8<-- "doc-snippets/config-starter-web.md"

For further information have a look at the [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#documentation).

??? info "Default configuration set by this library"
    ```properties
    --8<-- "sda-commons-starter-web/src/main/resources/org/sdase/commons/spring/boot/web/defaults.properties"

    --8<-- "sda-commons-starter-web/src/main/resources/org/sdase/commons/spring/boot/web/monitoring/monitoring.properties"

    --8<-- "sda-commons-starter-web/src/main/resources/org/sdase/commons/spring/boot/web/monitoring/tracing.properties"

    --8<-- "sda-commons-starter-web/src/main/resources/org/sdase/commons/spring/boot/web/client/default.properties"

    --8<-- "sda-commons-starter-web/src/main/resources/org/sdase/commons/spring/boot/web/docs/default.properties"
    ```

**Please make sure to configure `spring.application.name` for every service.**


## Authentication

- [Spring Security Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#web.security)

Enables feature that make a Spring Boot service compliant with the SDA SE Authentication concepts
using OIDC.

OIDC Authentication can be configured with `auth.issuers` to provide a comma separated
list of trusted issuers. In develop and test environments, the boolean `auth.disable` may
be used to disable authentication.

The JWKS URI of each issuer is updated when an unknown Key ID is received and every 5 minutes. The
cache of known JWK is invalidated after 15 minutes.

!!! warning
    This setup allows authenticated and anonymous requests! It is the responsibility of policies
    provided by the Open Policy Agent to decide about denying anonymous requests.

Spring Security is disabled for the Management/Admin Port (default: 8081). Be aware that these port
should not be accessible out of the deployment context.

This security implementation lacks some features compared to [sda-dropwizard-commons](https://github.com/SDA-SE/sda-dropwizard-commons/tree/main/sda-commons-server-auth):
- No configuration of static local public keys to verify the token signature. 
- No configuration of JWKS URIs to verify the token signature. 
- The IDP must provide an `iss` claim that matches the base URI for discovery. 
- Leeway is not configurable yet. 
- The client that loads the JWKS is not configurable yet.

## Authorization

Enables feature that make a Spring Boot service compliant with the SDA SE Authorization concepts
using Open Policy Agent.

The authorization is done by the [Open Policy Agent](https://www.openpolicyagent.org/). It can be 
configured with [`opa.` configuration properties](#configuration).

??? info "Requests to the Open Policy Agent"
    Requests to the server are determined by the base URL and the policy package.
    Given the default base URL `http://localhost:8181` and an example package of `com.my.service`,
    the Open Policy Agent server will be requested for authorization decision at
    `http://localhost:8181/v1/data/com/my/service`.

The OPA configuration acts as a client to the Open Policy Agent and is hooked in as request filter
which is part of the `SecurityFilterChain` including the OIDC Authentication.

Constraints provided with the Open Policy Agent response can be mapped to a custom POJO.
The class must extend `org.sdase.commons.spring.boot.web.auth.opa.AbstractConstraints` and must be
annotated with `org.sdase.commons.spring.boot.web.auth.opa.Constraints`.
It has request scope and can be [`@Autowired`](https://javadoc.io/doc/org.springframework/spring-beans/latest/org/springframework/beans/factory/annotation/Autowired.html) 
in [`@Controllers`](https://javadoc.io/doc/org.springframework/spring-webmvc/latest/org/springframework/web/servlet/mvc/Controller.html) 
or [`@RestControllers`](https://javadoc.io/doc/org.springframework/spring-web/latest/org/springframework/web/bind/annotation/RestController.html).

??? example "Custom Constraint implementation"
    === "com.example.my.service.rego"
        ```python
        package com.example.my.service
        
        import input
        
        # decode the token
        token = {"payload": payload} {
          not input.jwt = null
          io.jwt.decode(input.jwt, [_, payload, _])
        }
        
        # deny by default!
        default allow = false
        
        allow {
          # your rules
        }
        
        admin {
          token.payload.admin = "yes"
        }
        ```
    
    === "MyConstraints.java"
        ```java
        @Constraints
        public class MyConstraints extends AbstractConstraints {
        
          private boolean admin;
        
          public MyConstraints setAdmin(boolean admin) {
            this.admin = admin;
            return this;
          }
        
          public boolean isAdmin() {
            return admin;
          }
        }
        ```

    === "MyController.java"
        ```java
        @RestController
        public class MyController {
            @Autowired
            private MyConstraints myConstraints;
            // ...
        }
        ```

Additional parameters that are needed for the authorization decision may be provided with custom
`org.sdase.commons.spring.boot.web.auth.opa.extension.OpaInputExtension`s.

### Testing

The [testing module](web-testing.md#authentication-and-authorization) provides aligned test
dependencies including Wiremock for external APIs and JUnit extensions to mock or disable
authentication and authorization.

### OPA
![Overview](assets/overview_opa.svg)

The OPA configuration requests the policy decision providing the following inputs

| Property           | Description                                                                                           | Example                                                                              |
|--------------------|-------------------------------------------------------------------------------------------------------|--------------------------------------------------------------------------------------|
| `input.httpMethod` | HTTP method as uppercase string.                                                                      | `"GET"`                                                                              |
| `input.path`       | Requested path as array of path segments without context or servlet path.                             | `["myResource", "123-id", "someSubresource"]`                                        |
| `input.jwt`        | Validated encoded JWT as string (if available).                                                       | `"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIn0.Xpk63zUfXiI5f_bdGjqrhx03aGyBn9ETgXbkAgLalPk"` |
| `input.headers`    | All HTTP request headers as object with lower-case header names as key and array of headers as value. | `{"accept": "text/plain", "accept": "application/json"}`                             |

!!! warning "Security note"
    While a service might only consider one value of a specific header, that a policy might
    authorize on an array of those or vice versa.
    Consider this in your policy when you want to make sure you authorize on the same value that a
    service might use to evaluate the output.

??? info "Remark to HTTP request headers"  
    The configuration normalizes header names to lower case to simplify handling in OPA since HTTP
    specification defines header names as case-insensitive.
    Multivalued headers are not normalized with respect to the representation as list or single string
    with separator char.
    They are forwarded as parsed by the framework.

??? example "Example Policy Using Input"

    === "Policy"
        ```python
        # each policy lies in a package that is referenced in the configuration of the OpaBundle
        package example
        
        # decode the JWT as new variable 'token'
        token = {"payload": payload} {
            not input.jwt == null
            io.jwt.decode(input.jwt, [_, payload, _])
        }
        
        # deny by default
        default allow = false
        
        # the allow property is required for authorization
        allow {
            # allow if path match '/contracts/:anyid' 
            input.path = ["contracts", _]
        
            # allow if request method 'GET' is used
            input.httpMethod == "GET"
        
            # allow if 'claim' exists in the JWT payload
            token.payload.claim
        
            # allow if a request header 'HttpRequestHeaderName' has a certain value 
            input.headers["httprequestheadername"][_] == "certain-value"
        }
        
        # set some example constraints
        # constraints are always service dependent and the structure, if any, is defined in each service
        constraint1 := true                # always true
        constraint2 := [ "v2.1", "v2.2" ]  # always an array of "v2.1" and "v2.2"
        constraint3[token.payload.sub]     # always a set that contains the 'sub' claim from the token
                                           # or is empty if no token is present
        ```
    === "Result"
        ```json
        {
          "result": {
            "allow": true,
            "constraint1": true,
            "constraint2": [ "v2.1", "v2.2" ],
            "constraint3": ["my-sub"]
          }
        }
        ```


## Http Client

Enables support for [`org.springframework.cloud.openfeign.FeignClients`](https://javadoc.io/doc/org.springframework.cloud/spring-cloud-openfeign-core/3.1.6/index.html) 
that support SDA Platform features like:

  - passing the Authorization header to downstream services.
  - passing the Trace-Token header to downstream services.
  - OIDC client authentication

A feign client can be created as interface like this:
```java
@FeignClient(name = "partnerOds", url = "${partnerOds.baseUrl}")
public interface OtherServiceClient {
  @GetMapping("/partners")
  List<Partner> getPartners();
}
```
Then the spring boot application needs to be annotated with `@EnableFeignClients` in order for the component 
scanning to pick up the `@FeignClient` annotated interfaces like so
```java 
@EnableFeignClients
@SpringBootApplication
public class ExampleApplication { (...)
}
```

The Partner ODS base url must be configured as `http://partner-ods:8080/api` in the Spring
environment property `partnerOds.baseUrl`. Detailed configuration like timeouts can be configured
with [default feign properties](https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/#spring-cloud-feign-overriding-defaults) 
in the `application.yaml` or `derived environment` properties based on the `name` attribute of the 
[`org.springframework.cloud.openfeign.FeignClient`](https://javadoc.io/doc/org.springframework.cloud/spring-cloud-openfeign-core/3.1.6/index.html) 
annotation.

The client is then available as bean in the Spring context.

### Authentication forwarding
The client can be used within the SDA Platform to path through the received authentication header by
adding a configuration:
```java
@FeignClient(
  name = "partnerOds",
  url = "${partnerOds.baseUrl}",
  configuration = {AuthenticationPassThroughClientConfiguration.class}
)
public interface OtherServiceClient { 
  @GetMapping("/partners")
  List<Partner> getPartners();
}
```

`org.sdase.commons.spring.boot.web.client.AuthenticationPassThroughClientConfiguration` will take
the **Authorization** header from the current request context of the servlet and adds its value to
the client request.

### Trace-Token

The client can be used within the SDA Platform to pass through the received `Trace-Token` header by adding a configuration:

```java
@FeignClient(
  name = "partnerOds", 
  url = "${partnerOds.baseUrl}", 
  configuration = {SdaTraceTokenClientConfiguration.class}
)
public interface OtherServiceClient {
  @GetMapping("/partners") 
  List<Partner> getPartners();
}
```

`org.sdase.commons.spring.boot.web.tracing.SdaTraceTokenClientConfiguration` will take the
`Trace-Token` header from the current request context of the servlet and adds its value to the
client request.

If no `Trace-Token` header is present in the current request context, the
`SdaTraceTokenClientConfiguration` will generate a new Trace-Token and pass it to the following
requests.

### OIDC Client

If the request context is not always existing, e.g. in cases where a technical user for
service-to-service communication is required, the
`.org.sdase.commons.spring.boot.web.client.OidcClientRequestConfiguration` will request the required
OIDC authentication token with the client credentials flow using the configured
`"oidc.client.issuer.uri"`, `"oidc.client.id"` and `"oidc.client.secret"`.

If the current request context contains the **Authorization** header, the authentication
pass-through will be applied instead, in case `"oidc.client.authentication-passthrough.enabled"` flag is enabled.

### JAX-RS Mapping

If you would like to use JAX-RS based web annotations, you just need to apply
the `feign.jaxrs.JakartaContract.class` to configurations.

```java
@Path("customers")
@FeignClient(
    value = "customerService",
    url = "${customer.api.base.url}",
    configuration = {OidcClientRequestConfiguration.class, feign.jaxrs.JakartaContract.class})
public interface CustomerServiceApi {

  @POST
  @Path("/{customerId}/contracts")
  @Consumes(APPLICATION_JSON)
  void addContract(
      @PathParam("customerId") @NotBlank String customerId,
      Contract contract);
}

```

### Platform Client

The `PlatformClient` combines the authentication forwarding, trace token and OIDC configuration
without the need to configure each individually.

```java
@PlatformClient(
    value = "customerService",
    url = "${customer.api.base.url}")
public interface CustomerServiceApi {

  // ...
}
```

It abstracts some configuration of the FeignClient and is then available as bean as well.

## Error Handling

The module `sda-commons-starter-web` provides a shared `ApiError` model, to provide a common
response error structure for SDA-restful services.

### Usage

Per default, the module `sda-commons-starter-web` autoconfigures a
global `@ExceptionHandler(ApiException.class)` as `@ControllerAdvice`. As a result, the
exception handler is per default provided to every `@Controller`.

#### Referencing in OpenAPI

To provide the common `ApiError` in the API, you need to reference the class as `@Schema`.

```
@ApiResponse(
    responseCode = "422",
    description =
        "The request could not be processed due to invalid parameters. Details are provided in the error response.",
    content = @Content(schema = @Schema(implementation = ApiError.class)))
```

#### Throwing ApiException

When the `ApiException` is thrown the `@ExceptionHandler` automatically intercepts the exception and
maps the related `ResponseEntity`. As the result, the controller returns the related http response
code and the nested `ApiError`.

```
    throw ApiException.builder()
      .httpCode(422)
      .title("Invalid input")
      .detail("name", "name was not null", "NOT_NULL")
      .cause(e)
      .build();
```

In this example the controller would return with http status `422` and body:

```json
{
  "title": "Invalid input",
  "invalidParams": [
    {
      "field": "name",
      "reason": "name was not null",
      "errorCode": "NOT_NULL"
    }
  ]
}
```

## Async

The default Spring [async](https://javadoc.io/doc/org.springframework/spring-context/latest/org/springframework/scheduling/annotation/Async.html) 
task executor is autoconfigured to transfer the request attributes of the 
current request to the **Thread** running the asynchronous method.

## Jackson

Enables feature that makes a Spring Boot service compliant with the REST guide of SDA SE.
So far this covers:
- the tolerant reader pattern
- consistent serialization of `java.time.ZonedDateTime` compatible to the [type `date-time` of JSON-Schema](https://json-schema.org/understanding-json-schema/reference/string.html#dates-and-times).
  It is strongly recommended to use
  - `java.time.LocalDate` for dates without time serialized as `2018-09-23`
  - `java.time.ZonedDateTime` for date and times serialized as `2018-09-23T14:21:41+01:00`
  - `java.time.Duration` for durations with time resolution serialized as `P1DT13M`
  - `java.time.Period` for durations with day resolution serialized as `P1Y2D`

All these types can be read and written in JSON as ISO 8601 formats.
Reading `java.time.ZonedDateTime` is configured to be tolerant so that added nanoseconds or missing
milliseconds or missing seconds are supported.

`@com.fasterxml.jackson.annotation.JsonFormat(pattern = "...")` should not be used for customizing
serialization because it breaks tolerant reading of formatting variants. If a specific field should
be serialized with milliseconds, it must be annotated with
`@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = Iso8601Serializer.WithMillis.class)`
. If a specific field should be serialized with nanoseconds, it must be annotated with
`@com.fasterxml.jackson.databind.annotation.JsonSerialize(using = Iso8601Serializer.WithNanos.class)`

**Differences to the known [SDA Dropwizard Commons configuration](https://github.com/SDA-SE/sda-dropwizard-commons/tree/main/sda-commons-server-jackson)**
- `java.time.ZonedDateTime` fields are serialized with seconds by default.
  There is no other global configuration for **java.time.ZonedDateTime** serialization available.
- **Fewer modules are activated for foreign frameworks**. Compared to SDA Dropwizard Commons,
  **GuavaExtrasModule, JodaModule, and CaffeineModule** are not registered anymore.
- No documented customization of the global **com.fasterxml.jackson.databind.ObjectMapper** is available right now.
- Support for **HAL Links and embedding linked resources** is not implemented.
- Support for **YAML** is not implemented.
- There is **no support for field filters**.
  Such filters have been barely used in the SDA SE.

## Monitoring

Services use Prometheus to scrape and store metrics.

An actuator exposing metrics in prometheus format is available using the following endpoint

```
http://{serviceURL}:{adminPort}/metrics/prometheus
```

Spring Boot is using [micrometer](https://micrometer.io/) to instrument code using out-of-the-box bindings for common libraries.

### SDA specific metrics


| Metric Name          | Labels | Description                                           |
|----------------------|--------|-------------------------------------------------------|
| `healthcheck_status` | `name` | Exposes healthcheck as metric for multiple indicators |


### JVM and System metrics

| Metric Name                                  | Labels                  | Description                                                                                                                                                                       |
|----------------------------------------------|-------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `jvm_classes_loaded_classes`                 |                         | The number of classes that are currently loaded in the Java virtual machine.                                                                                                      |
| `jvm_classes_unloaded_classes`               |                         | The total number of classes unloaded since the Java virtual machine has started execution.                                                                                        |
| `jvm_buffer_count_buffers`                   | `id`                    | An estimate of the number of buffers in the pool.                                                                                                                                 |
| `jvm_buffer_memory_used_bytes`               | `id`                    | An estimate of the memory that the Java virtual machine is using for this buffer pool.                                                                                            |
| `jvm_buffer_total_capacity_bytes`            | `id`                    | An estimate of the total capacity of the buffers in this pool.                                                                                                                    |
| `jvm_memory_used_bytes`                      | `id`, `area`            | The amount of used memory.                                                                                                                                                        |
| `jvm_memory_committed_bytes`                 | `id`, `area`            | The amount of memory in bytes that is committed for the Java virtual machine to use.                                                                                              |
| `jvm_memory_max_bytes`                       | `id`, `area`            | The maximum amount of memory in bytes that can be used for memory management.                                                                                                     |
| `jvm_gc_max_data_size_bytes`                 |                         | Max size of long-lived heap memory pool.                                                                                                                                          |
| `jvm_gc_live_data_size_bytes`                |                         | Size of long-lived heap memory pool after reclamation.                                                                                                                            |
| `jvm_gc_memory_allocated_bytes_total`        |                         | Incremented for an increase in the size of the (young) heap memory pool after one GC to before the next.                                                                          |
| `jvm_gc_memory_promoted_bytes_total`         |                         | Count of positive increases in the size of the old generation memory pool before GC to after GC.                                                                                  |
| `jvm_gc_concurrent_phase_time_seconds_count` | `gc`, `action`, `cause` | Time spent in concurrent phase.                                                                                                                                                   |
| `jvm_gc_concurrent_phase_time_seconds_sum`   | `gc`, `action`, `cause` |                                                                                                                                                                                   |
| `jvm_gc_concurrent_phase_time_seconds_max`   | `gc`, `action`, `cause` |                                                                                                                                                                                   |
| `jvm_gc_pause_seconds_count`                 | `gc`, `action`, `cause` | Time spent in GC pause.                                                                                                                                                           |
| `jvm_gc_pause_seconds_sum`                   | `gc`, `action`, `cause` |                                                                                                                                                                                   |
| `jvm_gc_pause_seconds_max`                   | `gc`, `action`, `cause` |                                                                                                                                                                                   |
| `system_cpu_count`                           |                         | The number of processors available to the Java virtual machine.                                                                                                                   |
| `system_load_average_1m`                     |                         | The sum of the number of runnable entities queued to available processors and the number of runnable entities running on the available processors averaged over a period of time. |
| `system_cpu_usage`                           |                         | The "recent cpu usage" of the system the application is running in.                                                                                                               |
| `process_cpu_usage`                          |                         | The "recent cpu usage" for the Java Virtual Machine process.                                                                                                                      |
| `jvm_threads_peak_threads`                   |                         | The peak live thread count since the Java virtual machine started or peak was reset.                                                                                              |
| `jvm_threads_daemon_threads`                 |                         | The current number of live daemon threads.                                                                                                                                        |
| `jvm_threads_live`                           |                         | The current number of live threads including both daemon and non-daemon threads.                                                                                                  |
| `jvm_threads_started_threads_total`          |                         | The total number of application threads started in the JVM.                                                                                                                       |
| `jvm_threads_states_threads`                 | `state`                 | The current number of threads.                                                                                                                                                    |

### Key Metrics for monitoring Kafka

| Metric name                            | Labels                 | Description                                                                                                                           |
|----------------------------------------|------------------------|---------------------------------------------------------------------------------------------------------------------------------------|
| `kafka_producer_compression_rate_avg`  | `client.id`            | The average compression rate of record batches, defined as the average ratio of the compressed batch size over the uncompressed size. |
| `kafka_producer_response_rate`         | `client.id`            | The number of responses received per second                                                                                           |
| `kafka_producer_request_rate`          | `client.id`            | The number of requests sent per second                                                                                                |
| `kafka_producer_request_latency_avg`   | `client.id`            | The average request latency in ms                                                                                                     |
| `kafka_producer_outgoing_byte_rate`    | `client.id`            | The number of outgoing bytes sent to all servers per second                                                                           |
| `kafka_producer_io_wait_time_ns_avg`   | `client.id`            | The average length of time the I/O thread spent waiting for a socket ready for reads or writes in nanoseconds.                        |
| `kafka_producer_batch_size_avg`        | `client.id`            | The average number of bytes sent per partition per-request.                                                                           |
| `kafka_consumer_records_lag`           | `client.id`            | Number of messages consumer is behind producer on this partition                                                                      |
| `kafka_consumer_records_lag_max`       | `client.id`            | Maximum number of messages consumer is behind producer, either for a specific partition or across all partitions on this client       |
| `kafka_consumer_bytes_consumed_rate`   | `client.id`            | Average number of bytes consumed per second for a specific topic or across all topics.                                                |
| `kafka_consumer_records_consumed_rate` | `client.id`            | Average number of records consumed per second for a specific topic or across all topics                                               |
| `kafka_consumer_fetch_rate `           | `client.id`            | Number of fetch requests per second from the consumer                                                                                 |

### MongoDB metrics

| Metric name                             | Labels                                                             | Description                                                                    |
|-----------------------------------------|--------------------------------------------------------------------|--------------------------------------------------------------------------------|
| `mongodb_driver_pool_waitqueuesize`     | `cluster_id`, `server_address`                                     | The current size of the wait queue for a connection from the pool              |
| `mongodb_driver_pool_checkedout`        | `cluster_id`, `server_address`                                     | The count of connections that are currently in use                             |
| `mongodb_driver_pool_size`              | `cluster_id`, `server_address`                                     | The current size of the connection pool, including idle and and in-use members |
| `mongodb_driver_commands_seconds_max`   | `cluster_id`, `server_address`, `collection`, `command`, `status`  | Timer of mongodb commands                                                      |
| `mongodb_driver_commands_seconds_count` | `cluster_id`, `server_address`, `collection`, `command`, `status`  | Timer of mongodb commands                                                      |
| `mongodb_driver_commands_seconds_sum`   | `cluster_id`, `server_address`, `collection`, `command`, `status`  | Timer of mongodb commands                                                      |

## Tracing

Currently, tracing is leveraged by Micrometer Tracing and OpenTelemetry in the Spring context.
OpenTelemetry (OTEL) is a collection of standardized vendor-agnostic tools, APIs, and SDKs. It's a
CNCF incubating project and is a merger of the OpenTracing and OpenCensus projects.
OpenTracing is a vendor-neutral API for sending telemetry data over to an observability backend.
It uses Micrometer for code instrumentation & provide tracing bridge to OpenTelemetry and 
OpenTelemetry for tools to collect and send telemetry data to the reporter/collector.

Default features are:

* Adds trace and span ids to the Slf4J MDC, so you can extract all the logs from a given trace or
  span in a log aggregator.
* Instruments common ingress and egress points from Spring applications (servlet filter, rest
  template, scheduled actions, message channels, feign client).
* The service name is derived from `spring.application.name`
* Generate and report OTLP traces via HTTP or gRPC. By default, it sends them to a OTLP compatible
  collector (e.g. Jaeger) on localhost (http port 4317, gRPC port 4318). Configure the location of
  the service using `management.otlp.tracing.endpoint`.
* See [above](#configuration) for more common options.
* You can check all the possible values on [OtlpProperties](https://docs.spring.io/spring-boot/docs/current/api//org/springframework/boot/actuate/autoconfigure/metrics/export/otlp/OtlpProperties.html)
  and [TracingProperties](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/actuate/autoconfigure/tracing/TracingProperties.html)


## Health Checks / Actuator

Configures the Spring Boot Actuator to be accessible on root path `/` at default management
port `8081`.

The following endpoints are provided at the admin management endpoint:

- Liveness: `http://{serviceURL}:{adminPort}/healthcheck/liveness`
- Readiness: `http://{serviceURL}:{adminPort}/healthcheck/readiness`

The readiness group contains the following indicators:

- [`ReadinessStateHealthIndicator`](https://javadoc.io/doc/org.springframework.boot/spring-boot-actuator/latest/org/springframework/boot/actuate/availability/ReadinessStateHealthIndicator.html)
- [`MongoHealthIndicator`](https://javadoc.io/doc/org.springframework.boot/spring-boot-actuator/latest/org/springframework/boot/actuate/data/mongo/MongoHealthIndicator.html), if auto-configured.
- `OpenPolicyAgentHealthIndicator` if [OPA](#opa) is enabled for authentication

To overwrite the defaults [`HealthIndicator`](https://javadoc.io/doc/org.springframework.boot/spring-boot-actuator/latest/org/springframework/boot/actuate/health/HealthIndicator.html) of the readiness group, you can overwrite the property
source:

```properties
management.endpoint.health.group.readiness.include=readinessState, customCheck
```

Custom health indicators can be easily added to the application context:

```java
@Component
public class CustomHealthIndicator implements HealthIndicator {
  @Override
  public Health health() {
    return new Health.Builder().up().build();
  }
}
```

The custom health indicator will be available under `/healthcheck/custom` which is resolved by the
prefix of the [HealthIndicator](https://javadoc.io/doc/org.springframework.boot/spring-boot-actuator/latest/org/springframework/boot/actuate/health/HealthIndicator.html)
implementing component.


## Logging

The Spring Boot default logging is enabled.
Logs are printed to standard out.
`ENABLE_JSON_LOGGING=true` as environment variable or `-Denable.json.logging=true` as JVM parameter
enables output as JSON for structured logs used in log aggregation tools.
To enable JSON logging in `application.(properties/yaml)`,
`logging.config=classpath:org/sdase/commons/spring/boot/web/logging/logback-json.xml` may be used.

### Correlation of Logs in Distributed Systems

To allow correlation of logs in distributed systems a `Parent-Trace-Token` header is added to a 
Kafka message if a `Trace-Token` is present in the MDC of the producer.
A Kafka RecordInterceptor is used to read out the header and add the `Parent-Trace-Token` to the MDC
of the consumer service.
This allows the consumer service to log the `Parent-Trace-Token` and the current `Trace-Token`
to trace the origin.
**This only happens automatically if the used Listener is not a BatchListener**

## Metadata Context
If you want to make use of the data in the metadata context, you should read the [dedicated documentation](metadata-context.md).
If your service is required to support the metadata context but is not interested in the data,
continue here:

Services that use the sda-spring-boot-commons:

- can access the current `org.sdase.commons.spring.boot.metadata.context.MetadataContext` in their
  implementation
- will automatically load the context from incoming HTTP requests into the thread, handling the
  request, if you register `org.sdase.commons.spring.boot.web.metadata.MetadataContextConfiguration`
- will automatically load the context from consumed Kafka messages into the thread handling the
  message and the error when handling the message fails when the consumer is configured with one of
  the provided `org.sdase.commons.spring.boot.kafka.SdaKafkaConsumerConfiguration`
- will automatically propagate the context to other services via HTTP when using
  a `org.sdase.commons.spring.boot.web.client.PlatformClient`:
  ```java
  @PlatformClient(
    value = "name",
    url = "http://your-api-url"
  )
  public interface ClientWithMetadataConfiguration {
  @GetMapping("/metadata-hello")
    Object getSomething();
  }
  ```
- when using a FeignClient, that behaviour can be achieved by using
  the `org.sdase.commons.spring.boot.web.metadata.MetadataContextClientConfiguration`
  configuration.

  ```java
  @FeignClient(
    value = "name",
    url = "http://your-api-url",
    configuration = { MetadataContextClientConfiguration.class }
  )
  public interface ClientWithMetadataConfiguration {
    @GetMapping("/metadata-hello")
    Object getSomething();
  }
  ```

- will automatically propagate the context in produced Kafka messages when the producer is created
  with `org.sdase.commons.spring.boot.kafka.SdaKafkaProducerConfiguration`
- are configurable by the property or environment variable `METADATA_FIELDS` to be aware of the
  metadata used in a specific environment

Services that interrupt a business process should persist the context from
`MetadataContext.detachedCurrent()` and restore it with `MetadataContext.createContext(…)` when the
process continues.
Interrupting a business process means that processing is stopped and continued later in a new thread
or even another instance of the service.
Most likely, this will happen when a business entity is stored based on a request and loaded later
for further processing by a scheduler or due to a new user interaction.
In this case, the `DetachedMetadataContext` must be persisted along with the entity and recreated
when the entity is loaded.
The `DetachedMetadataContext` can be defined as field in any MongoDB entity.

For services that handle requests or messages in parallel, the metadata context attributes will 
be automatically transferred to the new threads, if `@Async` is used.
