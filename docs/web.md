# sda-commons-web-starter

The `sda-commons-web-starter` provides several features to provide a service based
on the SDA core concepts.

Features:
  - [Authentication](#authentication)
  - [Authorization](#authorization)
  - [Http Client](#http-client)
  - [Async Request Context](#async)
  - [Jackson Object Mapping](#jackson)
  - [Monitoring](#monitoring)
  - [Tracing](#tracing)
  - [Health Checks](#health-checks--actuator)
  - [Logging](#logging)
  - [Support for Metadata Context](#metadata-context)

Based on:
  - `org.springframework.boot:spring-boot-starter-web`
  - `org.springframework.boot:spring-boot-starter-oauth2-resource-server`
  - `org.springframework.boot:spring-boot-starter-oauth2-client`
  - `org.springframework.boot:spring-boot-starter-validation`
  - `org.springframework.boot:spring-boot-starter-actuator`
  - `org.springframework.cloud:spring-cloud-starter-openfeign`
  - `org.springframework.boot:spring-boot-starter-validation`
  - `org.springframework.cloud:spring-cloud-starter-sleuth`
  - `org.springframework.cloud:spring-cloud-sleuth-zipkin`

###  Configuration
| **Property**                             | **Description**                                                                                                                         | **Default**                                                          | **Example**                                                  | **Env**                         |
|------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|--------------------------------------------------------------|---------------------------------|
| `auth.issuers` _string_                  | Comma separated string of open id discovery key sources with required issuers.                                                          |                                                                      | `https://iam-int.dev.de/auth/realms/123`                     | `AUTH_ISSUERS`                  |
| `auth.disable` _boolean_                 | Disables authorization checks completely.                                                                                               | `false`                                                              | `true`                                                       | `AUTH_DISABLE`                  |
| `opa.disable` _boolean_                  | Disables authorization checks with Open Policy Agent completely.                                                                        | `false`                                                              | `true`                                                       | `OPA_DISABLE`                   |
| `opa.base.url` _string_                  | The baseUrl of OPA.                                                                                                                     | `http://localhost:8181`                                              | `http://opa-service:8181`                                    | `OPA_BASE_URL`                  |
| `opa.policy.package` _string_            | The policy package to check for authorization.                                                                                          | Defaults to package name of `@SpringBootApplication` annotated class | `com.custom.package.name`                                    | `OPA_POLICY_PACKAGE`            |
| `opa.exclude.patterns` _string_          | Custom excluded paths can be configured as comma separated list of regex.                                                               | `openapi.json` and `openapi.yaml `                                   | `/customPathOne,/customPathTwo`                              | `OPA_EXCLUDE_PATTERNS`          |
| `opa.client.connection.timeout` _string_ | The connection timeout of the client that calls the Open Policy Agent server.                                                           | `500ms`                                                              | `2s`                                                         | `OPA_CLIENT_CONNECTION_TIMEOUT` |
| `opa.client.timeout` _string_            | The read timeout of the client that calls the Open Policy Agent server.                                                                 | `500ms`                                                              | `2s`                                                         | `OPA_CLIENT_TIMEOUT`            |
| `oidc.client.enabled` _boolean_          | Enables OIDC Authentication (Client Credentials Flow) for the configured clients.                                                       | `false`                                                              | `true`                                                       | `OIDC_CLIENT_ENABLED`           |
| `oidc.client.id` _string_                | The client ID for the registration.                                                                                                     | ``                                                                   | `exampleClient`                                              | `OPA_CLIENT_ID`                 |
| `oid.client.secret` _string_             | The Client secret of the registration.                                                                                                  | ``                                                                   | `s3cret`                                                     | `OIDC_CLIENT_SECRET`            |
| `oidc.client.issuer.uri` _string_        | URI that can either be an OpenID Connect discovery endpoint or an OAuth 2.0 Authorization Server Metadata endpoint defined by RFC 8414. | ``                                                                   | `https://keycloak.sdadev.sda-se.io/auth/realms/exampleRealm` | `OIDC_CLIENT_ISSUER_URI`        |
| `cors.allowed-origin-patterns` _string_  | Comma separated list of URL patterns for which CORS requests are allowed.                                                               | _none allowed_                                                       | `https://*.all-subdomains.com, https://static-domain.com`    | `CORS_ALLOWEDORIGINPATTERNS`    |
| `request.body.max.size` _size_           | The maximum size allowed for request body data sent by a client.                                                                        | _1 MB_                                                               | `100 KB`, `10MB`                                             | `REQUEST_BODY_MAX_SIZE`         |
| `enable.json.logging` _boolean_          | If logs should be printed as JSON. _Note: This config param is not available for application.properties or application.yaml_            | _false_                                                              | `true`                                                       | `ENABLE_JSON_LOGGING`           |

For further information have a look at the [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#documentation).

## Web

The list of web configurations: 

- The`server.servlet.context-path` defaults to `/api`
- The`server.port` defaults to `8080`
- The`managment.server.port` defaults to `8081`
- The `openapi.yaml` is available under `/api/openapi.yaml`

**Please make sure to configure `spring.application.name` for every service**

## Authentication

- [Spring Security Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#web.security)

Enables feature that make a Spring Boot service compliant with the
[SDA SE Authentication](https://sda.dev/core-concepts/security-concept/authentication/) concepts
using OIDC.

OIDC Authentication can be configured with `auth.issuers` to provide a comma separated
list of trusted issuers. In develop and test environments, the boolean `auth.disable` may
be used to disable authentication.

The JWKS URI of each issuer is updated when an unknown Key ID is received and every 5 minutes. The
cache of known JWK is invalidated after 15 minutes.

**This setup allows authenticated and anonymous requests! It is the responsibility of policies
provided by the Open Policy Agent to decide about denying anonymous requests.**

Spring Security is disabled for the Management/Admin Port (default: 8081). Be aware that these port
should not be accessible out of the deployment context.

This security implementation lacks some features compared to [sda-dropwizard-commons](https://github.com/SDA-SE/sda-dropwizard-commons/tree/master/sda-commons-server-auth):
- No configuration of static local public keys to verify the token signature. 
- No configuration of JWKS URIs to verify the token signature. 
- The IDP must provide an `iss` claim that matches the base URI for discovery. 
- Leeway is not configurable yet. 
- The client that loads the JWKS is not configurable yet.

## Authorization

Enables feature that make a Spring Boot service compliant with the
[SDA SE Authorization](https://sda.dev/core-concepts/security-concept/authorization/) concepts
using Open Policy Agent.

The authorization is done by the [Open Policy Agent](https://www.openpolicyagent.org/). It can be 
configured as described in
[OpaAccessDecisionVoter#OpaAccessDecisionVoter
(boolean, String, String, OpaRequestBuilder, RestTemplate, ApplicationContext, io.opentelemetry.api.trace.Tracer)
](../../sda-commons-starter-web/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/OpaAccessDecisionVoter.java)
and [OpaRestTemplateConfiguration#OpaRestTemplateConfiguration(Duration, Duration)
](../../sda-commons-starter-web/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/OpaRestTemplateConfiguration.java).


The OPA configuration acts as a client to the Open Policy Agent and is hooked in as request filter (
Access Decision Manager) which is part of the `SecurityFilterChain` including the OIDC
Authentication.

Constraints provided with the Open Policy Agent response can be mapped to a custom POJO. If the
class extends [`AbstractConstraints`](../../sda-commons-starter-web/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/AbstractConstraints.java) 
and is annotated with [`@Constraints`](../../sda-commons-starter-web/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/Constraints.java) it can be
[`@Autowired`](https://javadoc.io/doc/org.springframework/spring-beans/latest/org/springframework/beans/factory/annotation/Autowired.html) 
in [`@Controllers`](https://javadoc.io/doc/org.springframework/spring-webmvc/latest/org/springframework/web/servlet/mvc/Controller.html) 
or [`@RestControllers`](https://javadoc.io/doc/org.springframework/spring-web/latest/org/springframework/web/bind/annotation/RestController.html).

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
```java
@RestController
public class AuthTestApp {
    @Autowired
    private MyConstraints myConstraints;
    // ...
}
```

Additional parameters that are needed for the authorization decision may be provided with custom
[OpaInputExtensions](../../sda-commons-starter-web/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/extension/OpaInputExtension.java).

### Testing

The testing module provides aligned test dependencies including Wiremock for external APIs and 
JUnit extensions to mock or disable authentication and authorization.

### OPA 
![Overview](assets/overview_opa.svg)

The OPA configuration requests the policy decision providing the following inputs

* HTTP path as Array
* HTTP method as String
* validated JWT (if available)
* all request headers

_Remark to HTTP request headers:_  
The configuration normalizes header names to lower case to simplify handling in OPA since HTTP
specification defines header names as case-insensitive.
Multivalued headers are not normalized with respect to the representation as list or single string
with separator char.
They are forwarded as parsed by the framework.

_Security note:_
Please be aware while a service might only consider one value of a specific header, the OPA is able
to authorize on a array of those.
Consider this in your policy when you want to make sure you authorize on the same value that a
service might use to evaluate the output.

These [inputs](../../sda-commons-starter-web/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/model/OpaInput.java)
can be accessed inside a policy `.rego`-file in this way:

```rego
# each policy lies in a package that is referenced in the configuration of the OpaBundle
package example

# decode the JWT as new variable 'token'
token = {"payload": payload} {
    not input.jwt == null
    io.jwt.decode(input.jwt, [_, payload, _])
}

# deny by default
default allow = false

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
constraint1 := true                # always true
constraint2 := [ "v2.1", "v2.2" ]  # always an array of "v2.1" and "v2.2"
constraint3[token.payload.sub]     # always a set that contains the 'sub' claim from the token
                                   # or is empty if no token is present

```

The response consists of two parts: The overall `allow` decision, and optional rules that represent _constraints_ to limit data access
within the service. These constraints are fully service dependent and MUST be applied when querying the database or
filtering received data.

The following listing presents a sample OPA result with a positive allow decision and two constraints, the first with boolean value and second
with a list of string values.
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

###  Configuration Properties

- `opa.disable` _boolean_
  - Disables authorization checks with Open Policy Agent completely. In this case access to all
    resources is granted but no constraints are provided.
- `opa.base.url` _string_
  - The base url of the Open Policy Agent Server. Defaults to `http://localhost:8181`.
    Requests to the server are determined by the base URL and the policy package. Given the default
    base URL `http://localhost:8181` and an example package of `com.my.service`, the Open Policy Agent
    server will be requested for authorization decision
    at `http://localhost:8181/v1/data/com/my/package`
- `opa.policy.package` _string_
  - The policy package to check for authorization. It will be reformatted to a URL path to request
    the authorization form the Open Policy Agent server. Example: `com.my.service`. If the policy
    package is blank, the package of the application class (the first bean found that is annotated
    with `@SpringBootApplication`) is used as a default. Be aware that moving the class causes a
    breaking change regarding deployment if the package is not explicitly set.
    Requests to the server are determined by the base URL and the policy package. Given the default
    base URL `http://localhost:8181` and an example package of `com.my.service`, the Open Policy Agent
    server will be requested for authorization decision
    at `http://localhost:8181/v1/data/com/my/package`
- `opa.exclude.patterns` _string_
  - `/openapi.yaml` and `/openapi.json` are excluded from authorization requirements. Custom excluded
    paths can be configured as comma separated list of regex. This will overwrite the default
    excludes of the OpenAPI documentation paths.
- `opa.client.timeout` _string_
  - The read timeout of the client that calls the Open Policy Agent server. Defaults to 500ms.
- `opa.client.connection.timeout` _string_
  - The connection timeout of the client that calls the Open Policy Agent server. Defaults to 500ms.

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

[`AuthenticationPassThroughClientConfiguration`](../../sda-commons-starter-web/src/main/java/org/sdase/commons/spring/boot/web/client/AuthenticationPassThroughClientConfiguration.java) 
will take the **Authorization** header from the current request context of the servlet and 
adds its value to the client request.

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

[SdaTraceTokenClientConfiguration](../../sda-commons-starter-web/src/main/java/org/sdase/commons/spring/boot/web/tracing/SdaTraceTokenClientConfiguration.java)
will take the `Trace-Token` header from the current request context of the servlet and adds its value to the client request.

If no `Trace-Token` header is present in the current request context, the [SdaTraceTokenClientConfiguration](../../sda-commons-starter-web/src/main/java/org/sdase/commons/spring/boot/web/tracing/SdaTraceTokenClientConfiguration.java)
will generate a new Trace-Token and pass it to the following requests.

### OIDC Client

If the request context is not always existing, e.g. in cases where a technical user for
service-to-service communication is required, the [`OidcClientRequestConfiguration`](../../sda-commons-starter-web/src/main/java/org/sdase/commons/spring/boot/web/client/OidcClientRequestConfiguration.java) 
will request the required OIDC authentication token with the client credentials flow using the 
configured `"oidc.client.issuer.uri"`, `"oidc.client.id"` and `"oidc.client.secret"`.

If the current request context contains the **Authorization** header, the authentication pass-through
will be applied instead.

### JAX-RS Mapping

If you would like to use JAX-RS based web annotations, you just need to apply
the `feign.jaxrs2.JAXRS2Contract.class` to configurations.

```java
@Path("customers")
@FeignClient(
    value = "customerService",
    url = "${customer.api.base.url}",
    configuration = {OidcClientRequestConfiguration.class, feign.jaxrs2.JAXRS2Contract.class})
public interface CustomerServiceApi {

  @POST
  @Path("/{customerId}/contracts")
  @Consumes(APPLICATION_JSON)
  void addContract(
      @PathParam("customerId") @NotBlank String customerId,
      Contract contract);
}

```

### Configuration properties

* `oidc.client.enabled` _boolean_
  * Enables OIDC Authentication (Client Credentials Flow) for the configured clients.
    If enabled, provide a client id, secret and an issuer url.
  * Example: `true`
  * Default: `false`
* `oidc.client.id` _string_
  * Client ID for the registration
  * Example: `oidcClient`
  * Default: ``
* `oid.client.secret` _string_
  * Client secret of the registration.
  * Example: `s3cret`
  * Default: ``
* `oidc.client.issuer.uri` _string_
  * URI that can either be an OpenID Connect discovery endpoint
    or an OAuth 2.0 Authorization Server Metadata endpoint defined by RFC 8414.
  * Example: `https://keycloak.sdadev.sda-se.io/auth/realms/exampleRealm`
  * Default: ``

### Platform Client

The Platform Client combines the authentication forwarding, trace token and OIDC configuration
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

The `sda-commons-web-starter` provides a shared `ApiError` model, to provide a common
response error structure for SDA-restful services.

### Usage

Per default, the `sda-commons-web-starter` autoconfigures a
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

Enables feature that make a Spring Boot service compliant with
the [SDA SE RESTful API Guide](https://sda.dev/core-concepts/communication/restful-api-guide/) .
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

**Differences to the known [SDA Dropwizard Commons configuration](https://github.com/SDA-SE/sda-dropwizard-commons/tree/master/sda-commons-server-jackson)**
- `java.time.ZonedDateTime` fields are serialized with seconds by default.
  There is no other global configuration for **java.time.ZonedDateTime** serialization available.
- **Less modules are activated for foreign frameworks**. Compared to SDA Dropwizard Commons,
  **GuavaExtrasModule, JodaModule, and CaffeineModule** are not registered anymore. 
- No documented customization of the global **com.fasterxml.jackson.databind.ObjectMapper** is available right now. 
- Support for **HAL Links and embedding linked resources** is not implemented. 
- Support for **YAML** is not implemented. 
- There is **no support for [field filters](https://sda.dev/core-concepts/communication/restful-api-guide/#RESTfulAPIGuide-MAY%3AProvidefieldfilteringtoretrievepartialresources)**.
  Such filters have been barely used in the SDA SE.

## Monitoring

TODO PROMETHEUS

Prometheus Metrics: `http://{serviceURL}:{adminPort}/metrics/prometheus`

### Default properties

```properties
# Metrics
management.endpoints.web.path-mapping.prometheus=metrics/prometheus
management.metrics.web.server.request.autotime.enabled=true
management.endpoint.prometheus.enabled=true
management.endpoint.metrics.enabled=true
```

## Tracing

Currently, tracing is leveraged by Micrometer Tracing rand OpenTelemetry in the Spring context.
OpenTelemetry (Otel) is a collection of standardized vendor-agnostic tools, APIs, and SDKs. It's a
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

* `management.otlp.tracing.endpoint` _string_
  * Base url to OTLP Collector instance.
  * Default: `http://localhost:4318`
* `management.tracing.enabled=false` _boolean_
  * For testing purposes it's maybe required to disable tracing. It is important to have also the
    annotation `@AutoConfigureObservability` on your class and in your tests to enable the tracing.
  * Example: `false`
  * Default: `true`
* `management.tracing.sampling.probability=0.20`
  * Probability in the range from 0.0 to 1.0 that a trace will be sampled.
  * Example: `0.20`
  * Default: `1.0`
* `management.tracing.propagation.type=b3`
  * Tracing context propagation types produced and consumed by the application. Setting this property overrides the more fine-grained propagation type properties.
  * Example: `b3`
  * Default: `b3,w3c`
* `management.tracing.grpc.enabled`
  * You only need to set this property to true if you want to use grpc (port 4317) vs http (port 4318) channel for span export.
  
You can check all the possible values on [OtlpProperties](https://docs.spring.io/spring-boot/docs/current/api//org/springframework/boot/actuate/autoconfigure/metrics/export/otlp/OtlpProperties.html)
and [TracingProperties](https://docs.spring.io/spring-boot/docs/current/api/org/springframework/boot/actuate/autoconfigure/tracing/TracingProperties.html)


### Default properties

```properties
management.otlp.tracing.endpoint=http://localhost:4318
management.tracing.enabled=true
management.tracing.propagation.type=b3,w3c
management.tracing.sampling.probability=1.0
```

## Health Checks / Actuator

Enable features that make a Spring Boot service compliant with
the [SDA SE Health Checks](https://sda.dev/developer-guide/deployment/health-checks/).

Configures the Spring Boot Actuator to be accessible on root path `/` at default management
port `8081`.

The following endpoints are provided at the admin management endpoint:

- Liveness: `http://{serviceURL}:{adminPort}/healthcheck/liveness`
- Readiness: `http://{serviceURL}:{adminPort}/healthcheck/readiness`

The readiness group contains the following indicators:

*   [`ReadinessStateHealthIndicator`](https://javadoc.io/doc/org.springframework.boot/spring-boot-actuator/latest/org/springframework/boot/actuate/availability/ReadinessStateHealthIndicator.html)
*   [`MongoHealthIndicator`](https://javadoc.io/doc/org.springframework.boot/spring-boot-actuator/latest/org/springframework/boot/actuate/data/mongo/MongoHealthIndicator.html), if auto-configured.
*   `OpenPolicyAgentHealthIndicator` if [OPA](#opa) is enabled for authentication

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

### Default properties

```properties
# Actuator
management.server.port=8081
management.server.base-path=/
management.endpoints.web.base-path=/
management.endpoints.web.exposure.include=*
management.endpoints.enabled-by-default=false
# Healthcheck
management.endpoint.health.enabled=true
management.endpoints.web.path-mapping.health=healthcheck
management.endpoint.health.probes.enabled=true
# Add the required auto configured health indicators which are supported in org.sdase.commons.spring
# See https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health.auto-configured-health-indicators
# to see the available indicators. If an included HealthIndicator is not autoconfigured, it will be automatically ignored
management.endpoint.health.group.readiness.include=readinessState, mongo
management.endpoint.health.show-details=always
management.endpoint.health.show-components=always
```

## Logging

The Spring Boot default logging is enabled.
Logs are printed to standard out.
`ENABLE_JSON_LOGGING=true` as environment variable or `-Denable.json.logging=true` as JVM parameter
enables output as JSON for structured logs used in log aggregation tools.
To enable JSON logging in `application.(properties/yaml)`,
`logging.config=classpath:org/sdase/commons/spring/boot/web/logging/logback-json.xml` may be used. 

## Metadata Context
If you want to make use of the data in the metadata context, you should read the [dedicated documentation](metadata-context.md).
If your service is required to support the metadata context but is not interested in the data,
continue here:

Services that use the sda-spring-boot-commons:
- can access the current [MetadataContext](../../sda-commons-metadata-context/src/main/java/org/sdase/commons/spring/boot/metadata/context/MetadataContext.java)
  in their implementation
- will automatically load the context from incoming HTTP requests into the thread handling the
  request, if you register [MetadataContextConfiguration](../../sda-commons-starter-web/src/main/java/org/sdase/commons/spring/boot/web/metadata/MetadataContextConfiguration.java)
- will automatically load the context from consumed Kafka messages into the thread handling the
  message and the error when handling the message fails when the consumer is configured with one of
  the
  provided [SdaKafkaConsumerConfiguration](../../sda-commons-starter-kafka/src/main/java/org/sdase/commons/spring/boot/kafka/SdaKafkaConsumerConfiguration.java)
- will automatically propagate the context to other services via HTTP when using a platform client
  that uses
  the [MetadataContextClientConfiguration](../../sda-commons-starter-web/src/main/java/org/sdase/commons/spring/boot/web/metadata/MetadataContextConfiguration.java)
  configuration, e.g:
  - ```java
    @FeignClient(
    value = "name",
    url = "http://your-api-url",
    configuration = {
      MetadataContextClientConfiguration.class
    })
    public interface ClientWithMetadataConfiguration {

    @GetMapping("/metadata-hello")
    Object getSomething();
    }
    ```
- will automatically propagate the context in produced Kafka messages when the producer is created
  with [SdaKafkaProducerConfiguration](../../sda-commons-starter-kafka/src/main/java/org/sdase/commons/spring/boot/kafka/SdaKafkaProducerConfiguration.java)
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
