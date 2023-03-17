# EnableSdaPlatform

It enables features that make a Spring Boot service compliant with the SDA SE Core Concepts.

So far this includes

- [EnableSdaRestGuide requirements of the RESTful API Guide](#enablesdarestguide)
- [EnableSdaDocs live documentation as OpenAPI 3](#enablesdadocs)
- [EnableSdaSecurity requirements of authentication and authorization](#enablesdasecurity)
- [EnableSdaClients REST clients configured for the SDA Platform](#enablesdaclients)
- [EnableSdaAsyncWithRequestContext keep the request context in async execution](#enablesdaasyncwithrequestcontext)
- [EnableSdaMonitoring requirements of the SDA Health checks](#enablesdamonitoring)
- [EnableSdaWebSecurity requirements of the RESTful API Guide](#enablesdawebsecurity)

Additionally, the default context path is configured as `/api` to provide consistent
resource paths in the SDA Platform. If special cases require to change this,
`server.servlet.context-path` can be overridden in the `application.yaml`

## EnableSdaRestGuide
It enable features that make a Spring Boot service compliant with the [SDA SE RESTful API Guide](https://sda.dev/core-concepts/communication/restful-api-guide/).

So far this covers:
* the tolerant reader pattern
* consistent serialization of `java.time.ZonedDateTime` compatible to the [type `date-time` of JSON-Schema](https://json-schema.org/understanding-json-schema/reference/string.html#dates-and-times). 
  
  It is strongly recommended to use
    * `java.time.LocalDate` for dates without time serialized as `2018-09-23`
    * `java.time.ZonedDateTime` for date and times serialized as `2018-09-23T14:21:41+01:00`
    * `java.time.Duration` for durations with time resolution serialized as `P1DT13M`
    * `java.time.Period` for durations with day resolution serialized as `P1Y2D`
  
  All these types can be read and written in JSON as ISO 8601 formats. 
  
  Reading `java.time.ZonedDateTime` is configured to be tolerant so that added nanoseconds or missing milliseconds or missing seconds are supported.
  
  `com.fasterxml.jackson.annotation.JsonFormat` **(pattern = "...") should not be used** 
  for customizing serialization because it breaks tolerant reading of formatting variants. 
  If a specific field should be serialized with milliseconds, it must be annotated with 
  `com.fasterxml.jackson.databind.annotation.JsonSerialize` **(using = Iso8601Serializer.WithMillis.class)**. 
  If a specific field should be serialized with nanoseconds, it must be annotated with 
  `com.fasterxml.jackson.databind.annotation.JsonSerialize` **(using = Iso8601Serializer.WithNano.class)**.

**Differences to the known [SDA Dropwizard Commons configuration](https://github.com/SDA-SE/sda-dropwizard-commons/tree/master/sda-commons-server-jackson)**
* `java.time.ZonedDateTime` fields are serialized with seconds by default. 
  There is no other global configuration for **java.time.ZonedDateTime** serialization available.
* **Less modules are activated for foreign frameworks**. Compared to SDA Dropwizard Commons, 
  **GuavaExtrasModule, JodaModule, AfterburnerModule and CaffeineModule** are not registered anymore.
* No documented customization of the global **com.fasterxml.jackson.databind.ObjectMapper** is available right now.
* Support for **HAL Links and embedding linked resources** is not implemented.
* Support for **YAML** is not implemented.
* There is **no support for [field filters](https://sda.dev/core-concepts/communication/restful-api-guide/#RESTfulAPIGuide-MAY%3AProvidefieldfilteringtoretrievepartialresources)**. 
  Such filters have been barely used in the SDA SE.

## EnableSdaDocs
Enables a customized version of OpenAPI3 docs that is served at `/openapi.json` and `/openapi.yaml`.

So far this covers:
* The **servers** tag is removed from the OpenAPI. 
* The OpenApi generation is deterministic.

## EnableSdaSecurity
It enables features that make a Spring Boot service compliant with the 
[SDA SE Authentication](https://sda.dev/core-concepts/security-concept/authentication/) and 
[SDA SE Authorization](https://sda.dev/core-concepts/security-concept/authorization/) 
concepts using OIDC and Open Policy Agent.

OIDC Authentication can be configured with `AUTH_ISSUERS` to provider a comma separated list of trusted issuers. 
In develop and test environments, the boolean `AUTH_DISABLE` may be used to disable authentication.

The JWKS URI of each issuer is updated when an unknown Key ID is received and every 5 minutes. 
The cache of known JWK is invalidated after 15 minutes.

**This setup allows authenticated and anonymous requests! 
It is the responsibility of policies provided by the Open Policy Agent to decide about denying anonymous requests.**

Authorization with the Open Policy Agent can be configured as described in 
[OpaAccessDecisionVoter#OpaAccessDecisionVoter(boolean, String, String, OpaRequestBuilder, RestTemplate, ApplicationContext, io.opentracing.Tracer)](sda-commons-web-autoconfigure/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/OpaAccessDecisionVoter.java) 
and [OpaRestTemplateConfiguration#OpaRestTemplateConfiguration(Duration, Duration)](sda-commons-web-autoconfigure/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/OpaRestTemplateConfiguration.java).

Constraints provided with the Open Policy Agent response can be mapped to a custom pojo. 
If the class extends [AbstractConstraints](../../sda-commons-web-autoconfigure/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/AbstractConstraints.java) 
and is annotated with [Constraints](../../sda-commons-web-autoconfigure/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/AbstractConstraints.java) 
it can be [org.springframework.beans.factory.annotation.Autowired](https://javadoc.io/doc/org.springframework/spring-beans/latest/org/springframework/beans/factory/annotation/Autowired.html) 
in [org.springframework.web.servlet.mvc.Controllers](https://javadoc.io/doc/org.springframework/spring-webmvc/latest/org/springframework/web/servlet/mvc/Controller.html) 
or [org.springframework.web.bind.annotation.RestControllers](https://javadoc.io/doc/org.springframework/spring-web/latest/org/springframework/web/bind/annotation/RestController.html).

Additional parameters that are needed for the authorization decision may be provided with custom 
[OpaInputExtensions](../../sda-commons-web-autoconfigure/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/extension/OpaInputExtension.java).

Testing `SpringBootTest` is supported by `DisableSdaAuth` and `EnableSdaAuthMock`.

`/openapi.yaml` and `/openapi.json` are excluded from authorization requirements.
Custom excluded paths can be configured as comma separated list of regex in `opa.exclude.patterns`. 
This will overwrite the default excludes of the OpenAPI documentation paths.

Spring Security is disabled for the Management/Admin Port (default: 8081). Be aware that these
port should not be accessible out of the deployment context.

This security implementation lacks some features compared to [sda-dropwizard-commons](https://github.com/SDA-SE/sda-dropwizard-commons/tree/master/sda-commons-server-auth):
* No configuration of static local public keys to verify the token signature. 
* No configuration of JWKS URIs to verify the token signature. 
* The IDP must provide an {@code iss} claim that matches the base URI for discovery. 
* Leeway is not configurable yet. 
* The client that loads the JWKS is not configurable yet.

## EnableSdaClients
Enables support for [org.springframework.cloud.openfeign.FeignClients](https://javadoc.io/doc/org.springframework.cloud/spring-cloud-openfeign-core/3.1.6/index.html) 
that support SDA Platform features like
* passing the Authorization header to downstream services. 
* passing the Trace-Token header to downstream services. 
* OIDC client authentication.

A feign client can be created as interface like this:

<pre>
  <code>
    @FeignClient(name = "partnerOds", url = "${partnerOds.baseUrl}")
    public interface OtherServiceClient {
      @GetMapping("/partners")
      List<Partner> getPartners();
    }
  </code>
</pre>

The client is then available as bean in the Spring context.

The Partner ODS base url must be configured as `http://partner-ods:8080/api` in the Spring environment 
property `partnerOds.baseUrl`. Detailed configuration like timeouts can be configured with [default feign properties](https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/#spring-cloud-feign-overriding-defaults)
in the `application.yaml` or derived environment properties based on the `name` attribute of the [org.springframework.cloud.openfeign.FeignClient](https://javadoc.io/doc/org.springframework.cloud/spring-cloud-openfeign-core/3.1.6/index.html) 
annotation.

The client can be used within the SDA Platform to path through the received authentication header by adding a configuration:

<pre>
  <code>
    @FeignClient(name = "partnerOds", url = "${partnerOds.baseUrl}", configuration = {AuthenticationPassThroughClientConfiguration.class)
    public interface OtherServiceClient {
      @GetMapping("/partners")
      List&lt;Partner&gt; getPartners();
    }
  </code>
</pre>

[AuthenticationPassThroughClientConfiguration](../../sda-commons-web-autoconfigure/src/main/java/org/sdase/commons/spring/boot/web/client/AuthenticationPassThroughClientConfiguration.java)
will take the **Authorization** header from the current request context of the servlet and adds its value to the client request.

If the request context is not always existing, e.g. in cases where a technical user for 
service-to-service communication is required, the [OidcClientRequestConfiguration](../../sda-commons-web-autoconfigure/src/main/java/org/sdase/commons/spring/boot/web/client/OidcClientRequestConfiguration.java) 
will request the required OIDC authentication token with the client credentials flow using the configured 
`"oidc.client.issuer.uri"`, `"oidc.client.id"` and `"oidc.client.secret"`. If the current request context 
contains the **Authorization** header, the authentication pass-through will be applied instead.

The client can be used within the SDA Platform to path through the received Trace-Token header by adding a configuration:

<pre>
  <code>
  @FeignClient(name = "partnerOds", url = "${partnerOds.baseUrl}", configuration = {SdaTraceTokenClientConfiguration.class})
  public interface OtherServiceClient {
    @GetMapping("/partners")
    List&lt;Partner&gt; getPartners();
  }
  </code>
</pre>

[SdaTraceTokenClientConfiguration](../../sda-commons-web-autoconfigure/src/main/java/org/sdase/commons/spring/boot/web/tracing/SdaTraceTokenClientConfiguration.java) 
will take the `Trace-Token` header from the current request context of the servlet and adds its value to the client request.

If no `Trace-Token` header is present in the current request context, the [SdaTraceTokenClientConfiguration](../../sda-commons-web-autoconfigure/src/main/java/org/sdase/commons/spring/boot/web/tracing/SdaTraceTokenClientConfiguration.java)
will generate a new Trace-Token and pass it to the following requests.

## EnableSdaAsyncWithRequestContext
Configures Spring's [org.springframework.scheduling.annotation.Async](https://javadoc.io/doc/org.springframework/spring-context/latest/org/springframework/scheduling/annotation/Async.html)
executor to transfer the request attributes of the current request to the **Thread** running the asynchronous method.

[Constraints](../../sda-commons-web-autoconfigure/src/main/java/org/sdase/commons/spring/boot/web/auth/opa/Constraints.java) 
from [EnableSdaSecurity](#enablesdasecurity) are available in the asynchronous **Thread** as well.

## EnableSdaMonitoring

### Monitoring
Enable features that make a Spring Boot service compliant with the [SDA SE Health Checks](https://sda.dev/developer-guide/deployment/health-checks/).

Configures the Spring Boot Actuator to be accessible on root path `"/"` at default management port `8081`.

Request for healtchecks on `/healthcheck/{*pathToIndicatorOrGroup}`
* Request for liveness probe on `"/healthcheck/liveness"`
* Request for readiness probe on `"/healthcheck/readiniess"`.

The readiness group contains the following indicators:
* [ReadinessStateHealthIndicator](https://javadoc.io/doc/org.springframework.boot/spring-boot-actuator/latest/org/springframework/boot/actuate/availability/ReadinessStateHealthIndicator.html)
* [MongoHealthIndicator](https://javadoc.io/doc/org.springframework.boot/spring-boot-actuator/latest/org/springframework/boot/actuate/data/mongo/MongoHealthIndicator.html), 
  if auto-configured.

To overwrite the defaults [HealthIndicator](https://javadoc.io/doc/org.springframework.boot/spring-boot-actuator/latest/org/springframework/boot/actuate/health/HealthIndicator.html)
of the readiness group, you can overwrite the property source `"management.endpoint.health.group.readiness.include=readinessState, customCheck"`.

Custom health indicators can be easily added to the application context:

<pre>
  <code>
  @Component public class CustomHealthIndicator implements HealthIndicator {
    @Override
    public Health health() {
       return new Health.Builder().up().build();
    }
  }
  </code>
</pre>

The custom health indicator will be available under `"/healthcheck/custom"` which is resolved by 
the prefix of the [HealthIndicator](https://javadoc.io/doc/org.springframework.boot/spring-boot-actuator/latest/org/springframework/boot/actuate/health/HealthIndicator.html) 
implementing component.

**TODO METRIC / PROMETHEUS**

### Tracing
Currently, tracing is leveraged by Sleuth in the Spring context. Spring Cloud Sleuth provides 
Spring Boot auto-configuration for distributed tracing. Sleuth was built around Zipkin traces and 
so only supports forwarding them to Zipkin (Thrift via Brave) format for now. But since Jaeger 
supports Zipkin traces and the OpenTracing Jaager Spring support is not heavily maintained, there 
is a need to stick with Sleuth. However, Spring Sleuth is compatible with OpenTracing, so we can 
use the standardized interfaces, hence the OpenTracing [io.opentracing.Tracer](https://javadoc.io/doc/io.opentracing/opentracing-api/0.32.0/io/opentracing/Tracer.html) is on classpath.

Even if Jaeger supports the Zipkin B3 propagation format, Sleuth is forced to just use per 
default the [W3C context propagation](https://www.w3.org/TR/trace-context/)

Default features are:
* Adds trace and span ids to the Slf4J MDC, so you can extract all the logs from a given trace or span in a log aggregator. 
* Instruments common ingress and egress points from Spring applications (servlet filter, rest template, scheduled actions, message channels, feign client). 
* The service name is derived from `spring.application.name`
* Generate and report Jaeger-compatible traces via HTTP. By default, it sends them to a Zipkin collector on localhost (port 9411). 
  Configure the location of the service using `spring.zipkin.base-url`.

## EnableSdaWebSecurity