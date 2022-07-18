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
  - [Health Checks](#health-checks)
  - [Testing](#testing)
  - [Logging](#logging)

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

For further information have a look at the [Spring Boot documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#documentation).

## Web

The list of web configurations: 

- The`server.servlet.context-path` defaults to `/api`
- The`server.port` defaults to `8080`
- The`managment.server.port` defaults to `8081`
- The `openapi.yaml` is available under `/api/openapi.yaml`
- To enable [JSON logging](../../sda-commons-web-autoconfigure/src/main/resources/org/sdase/commons/spring/boot/web/logging/logback-json.xml)
  you need to set the property `logging.config`to `classpath:org/sdase/commons/spring/logging/logback-json.xml`
  If not set, default console Spring logging is active.

**Please make sure to configure `spring.application.name` for every service**

## Authentication

- [Spring Security Documentation](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#web.security)

Enables features that make a Spring Boot service compliant with the
[SDA SE Authentication](https://sda.dev/core-concepts/security-concept/authentication/)
and [SDA SE Authorization](https://sda.dev/core-concepts/security-concept/authorization/) concepts
using OIDC and Open Policy Agent.

OIDC Authentication can be configured with `auth.issuers` to provide a comma separated
list of trusted issuers. In develop and test environments, the boolean `auth.disable` may
be used to disable authentication.

The JWKS URI of each issuer is updated when an unknown Key ID is received and every 5 minutes. The
cache of known JWK is invalidated after 15 minutes.

**This setup allows authenticated and anonymous requests! It is the responsibility of policies
provided by the Open Policy Agent to decide about denying anonymous requests.**

Spring Security is disabled for the Management/Admin Port (default: 8081). Be aware that these port
should not be accessible out of the deployment context.

## Authorization

The authorization is done by the [Open Policy Agent](https://www.openpolicyagent.org/). 

The OPA configuration acts as a client to the Open Policy Agent and is hooked in as request filter (
Access Decision Manager) which is part of the `SecurityFilterChain` including the OIDC
Authentication.

Constraints provided with the Open Policy Agent response can be mapped to a custom POJO. If the
class extends `AbstractConstraints` and is annotated with `@Constraints` it can be
`@Autowired` in `@Controllers` or `@RestControllers`.

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

  @Autowired private MyConstraints myConstraints;
  ...
```

### Testing

TODO

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

These [inputs](../../sda-commons-web-autoconfigure/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/model/OpaInput.java)
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
constraint3[token.payload.sub].    # always a set that contains the 'sub' claim from the token
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

Enables support for `org.springframework.cloud.openfeign.FeignClients` that support SDA Platform
features like:

  - passing the Authorization header to downstream services.
  - OIDC client authentication

A feign client can be created as interface like this:
```java
@FeignClient(name = "partnerOds", url = "${partnerOds.baseUrl}")
public interface OtherServiceClient {
  @GetMapping("/partners")
  List<Partner> getPartners();
}
```

The Partner ODS base url must be configured as `http://partner-ods:8080/api` in the Spring
environment property `partnerOds.baseUrl`. Detailed configuration like timeouts can be configured
with default feign properties in the `application.yaml` or `derived environment` properties based on
the name attribute of the `org.springframework.cloud.openfeign.FeignClient `annotation.

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

`AuthenticationPassThroughClientConfiguration` will take the Authorization header from the current
request context of the servlet and adds its value to the client request.

### OIDC Client

If the request context is not always existing, e.g. in cases where a technical user for
service-to-service communication is required, the `OidcClientRequestConfiguration` will request the
required OIDC authentication token with the client credentials flow.

If the current request context contains the Authorization header, the authentication pass-through
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

In this example the controler would return with http status `422` and body:

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

The default Spring async task executor is autoconfigured to transfer the request attributes of the
current request to the thread running the asynchronous method.

## Jackson

Enables features that make a Spring Boot service compliant with
the [SDA SE RESTful API Guide](https://sda.dev/core-concepts/communication/restful-api-guide/) .
So far this covers:
- the tolerant reader pattern
- consistent serialization of java.time.ZonedDateTime compatible to the type date-time of JSON-Schema.
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

Currently, tracing is leveraged by Sleuth in the Spring context. Spring Cloud Sleuth provides Spring
Boot autoconfiguration for distributed tracing. Sleuth was built around Zipkin traces and so only
supports forwarding them to Zipkin (Thrift via Brave) format for now. But since Jaeger supports
Zipkin traces and the OpenTracing Jaeger Spring support is not heavily maintained, there is a need
to stick with Sleuth. However, Spring Sleuth is compatible with OpenTracing, so we can use the
standardized interfaces, hence the OpenTracing {@linkplain io.opentracing.Tracer} is on classpath.

Even if Jaeger supports the Zipkin B3 propagation format, Sleuth is forced to just use per default
the [W3C context propagation](https://www.w3.org/TR/trace-context)

Default features are:

* Adds trace and span ids to the Slf4J MDC, so you can extract all the logs from a given trace or
  span in a log aggregator.
* Instruments common ingress and egress points from Spring applications (servlet filter, rest
  template, scheduled actions, message channels, feign client).
* The service name is derived from {@code spring.application.name}
* Generate and report Jaeger-compatible traces via HTTP. By default it sends them to a Zipkin
  collector on localhost (port 9411). Configure the location of the service using {@code
  spring.zipkin.base-url}


* `spring.zipkin.base.url` _string_
  * Base url to Zipkin or Zipkin Collector of Jaeger instance.
    In case of Jaeger, the Zipkin collector
    must be enabled manually.
  * Example: `http://jeager:9411`
  * Default: `http://localhost:9411`
* `spring.zipkin.enabled` _boolean_
  * For testing purposes it's may required to disable tracing.
  * Example: `false`
  * Default: `true`

### Default properties

```properties
spring.sleuth.propagation.type=W3C, B3
spring.sleuth.opentracing.enabled=true
```

## Health Checks / Actuator

Enables features that make a Spring Boot service compliant with
the [SDA SE Health Checks](https://sda.dev/developer-guide/deployment/health-checks/).

Configures the Spring Boot Actuator to be accessible on root path `/` at default management
port `8081`.

The following endpoints are provided at the admin management endpoint:

- Liveness: `http://{serviceURL}:{adminPort}/healthcheck/liveness`
- Readiness: `http://{serviceURL}:{adminPort}/healthcheck/readiness`

The readiness group contains the following indicators:

*   `ReadinessStateHealthIndicator`
*   `MongoHealthIndicator`, if auto-configured.

To overwrite the defaults `HealthIndicator` of the readiness group, you can overwrite the property
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
prefix of the HealthIndicator implementing component.

### Default properties

```properties
# Actuator
management.server.port=8081
management.server.servlet.context-path=/
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

The Spring Boot default is enabled. 

### Configuration properties

* `logging.config` _string_
  * Path to the logback configuration on classpath. If not set, default console Spring logging is
    active.
    Available custom configurations on classpath:
    * `classpath:org/sdase/commons/spring/logging/logback-json.xml` for Json Logging
  * Example: `classpath:org/sdase/commons/spring/logging/logback-json.xml`
  * Default: `org/springframework/boot/logging/logback/defaults.xml`


## Testing