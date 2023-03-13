## EnableSdaPlatform

It enables features that make a Spring Boot service compliant with the SDA SE Core Concepts.

So far this includes

- [EnableSdaRestGuide requirements of the RESTful API Guide](#enablesdarestguide)
- [EnableSdaDocs live documentation as OpenAPI 3](#enablesdadocs)
- [EnableSdaSecurity requirements of authentication and authorization](#enablesdasecurity)
- [EnableSdaClients REST clients configured for the SDA Platform](#enablesdaclients)
- [EnableSdaAsyncWithRequestContext keep the request context in async execution](#enablesdaasyncwithrequestcontext)

Additionally, the default context path is configured as `/api` to provide consistent
resource paths in the SDA Platform. If special cases require to change this,
`server.servlet.context-path` can be overridden in the `application.yaml`

## EnableSdaRestGuide

 Enables features that make a Spring Boot service compliant with the <a
 href="https://sda.dev/core-concepts/communication/restful-api-guide/">SDA SE RESTful API
 Guide</a>.

 <p>So far this covers:
  <ul>

<li>the tolerant reader pattern</li>
  <li>consistent serialization of `java.time.ZonedDateTime` compatible to
    the [type date-time of JSON-Schema](https://json-schema.org/understanding-json-schema/reference/string.html#dates-and-times)</li>
    <p>It is strongly recommended to use
      <ul>
        <li><strong>java.time.LocalDate</strong> for dates without time serialized as <strong>2018-09-23</strong></li>
        <li><strong>java.time.ZonedDateTime</strong> for date and times serialized as
          <strong>2018-09-23T14:21:41+01:00</strong></li>
        <li><strong>java.time.Duration</strong> for durations with time resolution serialized as <strong>P1DT13M</strong></li>
        <li><strong>java.time.Period</strong> for durations with day resolution serialized as <strong>P1Y2D</strong></li>
      </ul>
      <p>All these types can be read and written in JSON as ISO 8601 formats.</p>
      <p>Reading `java.time.ZonedDateTime` is configured to be tolerant so that added
      nanoseconds or missing milliseconds or missing seconds are supported.</p>
      <p><strong>com.fasterxml.jackson.annotation.JsonFormat (pattern = "...")</strong>
        <b>should not be used</b> for customizing serialization because it breaks
        tolerant reading of formatting variants. If a specific field should be serialized with
        milliseconds, it must be annotated with <strong>
        com.fasterxml.jackson.databind.annotation.JsonSerialize (using = Iso8601Serializer.WithMillis.class)</strong>.
        If a specific field should be serialized with
        nanoseconds, it must be annotated with <strong>
        com.fasterxml.jackson.databind.annotation.JsonSerialize (using =
        Iso8601Serializer.WithNano.class)</strong>.
      </p>
  </ul>

 <p><strong>Differences to the known <a
 href="https://github.com/SDA-SE/sda-dropwizard-commons/tree/master/sda-commons-server-jackson">SDA
 Dropwizard Commons configuration</a></strong>

 <ul>
    <li><strong>java.time.ZonedDateTime</strong> fields are serialized with seconds by
        default. There is no other global configuration for <strong>java.time.ZonedDateTime</strong>
        serialization available.</li>
    <li><strong>Less modules are activated for foreign frameworks</strong>. Compared to SDA
        Dropwizard Commons, <strong>GuavaExtrasModule</strong>, <strong>JodaModule</strong>, <strong>AfterburnerModule</strong> and <strong>CaffeineModule</strong> are not registered anymore.</li>
    <li>No documented customization of the global <strong>com.fasterxml.jackson.databind.ObjectMapper</strong> is available right now.</li>
    <li>Support for <strong>HAL Links and embedding linked resources</strong> is not implemented
        yet.</li>
    <li>Support for <strong>YAML</strong> is not implemented yet.</li>
    <li>There is <strong>no support for <a
        href="https://sda.dev/core-concepts/communication/restful-api-guide/#RESTfulAPIGuide-MAY%3AProvidefieldfilteringtoretrievepartialresources">field
        filters</a></strong>. Such filters have been barely used in the SDA SE.</li>
  </ul>

## EnableSdaDocs
 Enables a customized version of OpenAPI3 docs that is served at `/openapi.json` and
 `/openapi.yaml`.

 <p>So far this covers:

 <ul>
   <li>The <strong>servers</strong> tag is removed from the OpenAPI.
   <li>The OpenApi generation is deterministic.
 </ul>

## EnableSdaSecurity

 It enables features that make a Spring Boot service compliant with the <a
 href="https://sda.dev/core-concepts/security-concept/authentication/">SDA SE Authentication</a>
 and <a href="https://sda.dev/core-concepts/security-concept/authorization/">SDA SE
 Authorization</a> concepts using OIDC and Open Policy Agent.

 <p>OIDC Authentication can be configured with <strong>AUTH_ISSUERS</strong> to provider a comma separated
 list of trusted issuers. In develop and test environments, the boolean <strong>AUTH_DISABLE</strong> may
 be used to disable authentication.</p>

 <p>The JWKS URI of each issuer is updated when an unknown Key ID is received and every 5 minutes.
 The cache of known JWK is invalidated after 15 minutes.</p>

 <p><strong>This setup allows authenticated and anonymous requests! It is the responsibility of
 policies provided by the Open Policy Agent to decide about denying anonymous requests.</strong></p>

 <p>Authorization with the Open Policy Agent can be configured as described in <strong>
 OpaAccessDecisionVoter#OpaAccessDecisionVoter(boolean, String, String, OpaRequestBuilder,
 RestTemplate, ApplicationContext, io.opentracing.Tracer)</strong> and <strong>
 OpaRestTemplateConfiguration#OpaRestTemplateConfiguration(Duration, Duration)</strong>.</p>

 <p>Constraints provided with the Open Policy Agent response can be mapped to a custom pojo. If
 the class extends <strong> AbstractConstraints</strong> and is annotated with <strong> Constraints</strong> it can be
 <strong> org.springframework.beans.factory.annotation.Autowired</strong> in <strong>
 org.springframework.web.servlet.mvc.Controller</strong>s or <strong>
 org.springframework.web.bind.annotation.RestController</strong>.</p>

 <p>Additional parameters that are needed for the authorization decision may be provided with
 custom <strong> OpaInputExtension</strong>.</p>

 <p>Testing <strong>SpringBootTest</strong>s is supported by <strong>DisableSdaAuth</strong> and <strong>
 EnableSdaAuthMock</strong>.</p>

 <p><strong>/openapi.yaml</strong> and <strong>/openapi.json</strong> are excluded from authorization requirements.
 Custom excluded paths can be configured as comma separated list of regex in <strong>
 opa.exclude.patterns</strong>. This will overwrite the default excludes of the OpenAPI documentation
 paths.</p>

 <p>Spring Security is disabled for the Management/Admin Port (default: 8081). Be aware that these
 port should not be accessible out of the deployment context.</p>

 <p>This security implementation lacks some features compared to <a
 href="https://github.com/SDA-SE/sda-dropwizard-commons/tree/master/sda-commons-server-auth">sda-dropwizard-commons</a>:

 <ul>
   <li>No configuration of static local public keys to verify the token signature.</li>
   <li>No configuration of JWKS URIs to verify the token signature.</li>
   <li>The IDP must provide an <strong>iss</strong> claim that matches the base URI for discovery.</li>
   <li>Leeway is not configurable yet.</li>
   <li>The client that loads the JWKS is not configurable yet.</li>
 </ul>

## EnableSdaClients
 Enables support for <strong> org.springframework.cloud.openfeign.FeignClient</strong>s that support SDA
 Platform features like

 <ul>
   <li>passing the Authorization header to downstream services.
   <li>passing the Trace-Token header to downstream services.
   <li>OIDC client authentication.
 </ul>

 <p>A feign client can be created as interface like this:

 <pre>
   <code>@FeignClient(name = "partnerOds", url = "${partnerOds.baseUrl}")
    public interface OtherServiceClient {
      @GetMapping("/partners")
      List&lt;Partner&gt; getPartners();
    }
   </code>
 </pre>
 <p>The client is then available as bean in the Spring context.

 <p>The Partner ODS base url must be configured as <strong> http://partner-ods:8080/api</strong> in the
 Spring environment property <strong> partnerOds.baseUrl</strong>. Detailed configuration like timeouts can
 be configured with <a
 href="https://docs.spring.io/spring-cloud-openfeign/docs/current/reference/html/#spring-cloud-feign-overriding-defaults">default
 feign properties</a> in the <strong> application.yaml</strong> or derived environment properties based on
 the <strong> name</strong> attribute of the <strong> org.springframework.cloud.openfeign.FeignClient</strong>
 annotation.</p>

 <p>The client can be used within the SDA Platform to path through the received authentication
 header by adding a configuration:

 <pre>
   <code>
      @FeignClient(
       name = "partnerOds",
       url = "${partnerOds.baseUrl}",
       configuration = {AuthenticationPassThroughClientConfiguration.class)
       public interface OtherServiceClient {
         @GetMapping("/partners")
         List&lt;Partner&gt; getPartners();
       }
   </code>
 </pre>

 <strong> AuthenticationPassThroughClientConfiguration</strong> will take the <strong> Authorization</strong> header
 from the current request context of the servlet and adds its value to the client request.

 <p>If the request context is not always existing, e.g. in cases where a technical user for
 service-to-service communication is required, the <strong> OidcClientRequestConfiguration</strong> will
 request the required OIDC authentication token with the client credentials flow using the
 configured <strong> "oidc.client.issuer.uri"</strong>, <strong> "oidc.client.id"</strong> and <strong>
 "oidc.client.secret"</strong>. If the current request context contains the <strong> Authorization</strong> header,
 the authentication pass-through will be applied instead.

 <p>The client can be used within the SDA Platform to path through the received Trace-Token header
 by adding a configuration:

 <pre>
   <code>@FeignClient(
     name = "partnerOds",
     url = "${partnerOds.baseUrl}",
     configuration = {SdaTraceTokenClientConfiguration.class})
    public interface OtherServiceClient {
      @GetMapping("/partners")
      List&lt;Partner&gt; getPartners();
    }
   </code>
 </pre>

 <strong> SdaTraceTokenClientConfiguration</strong> will take the <strong> Trace-Token</strong> header from the
 current request context of the servlet and adds its value to the client request.

 <p>If no Trace-Token header is present in the current request context, the <strong>
 SdaTraceTokenClientConfiguration</strong> will generate a new Trace-Token and pass it to the following
 requests.

## EnableSdaAsyncWithRequestContext
 Configures Springs <strong>org.springframework.scheduling.annotation.Async</strong> executor to transfer
 the request attributes of the current request to the <strong>Thread</strong> running the asynchronous
 method.

 <p><strong>Constraints</strong> from <strong>@EnableSdaSecurity</strong> are available in the asynchronous <strong>
 Thread</strong> as well.

### EnableSdaWebSecurity

### EnableSdaSecurity
 Enables features that make a Spring Boot service compliant with the <a
 href="https://sda.dev/core-concepts/security-concept/authentication/">SDA SE Authentication</a>
 and <a href="https://sda.dev/core-concepts/security-concept/authorization/">SDA SE
 Authorization</a> concepts using OIDC and Open Policy Agent.

 <p>OIDC Authentication can be configured with {@code AUTH_ISSUERS} to provider a comma separated
 list of trusted issuers. In develop and test environments, the boolean {@code AUTH_DISABLE} may
 be used to disable authentication.

 <p>The JWKS URI of each issuer is updated when an unknown Key ID is received and every 5 minutes.
 The cache of known JWK is invalidated after 15 minutes.

 <p><strong>This setup allows authenticated and anonymous requests! It is the responsibility of
 policies provided by the Open Policy Agent to decide about denying anonymous requests.</strong>

 <p>Authorization with the Open Policy Agent can be configured as described in {@link
 OpaAccessDecisionVoter#OpaAccessDecisionVoter(boolean, String, String, OpaRequestBuilder,
 RestTemplate, ApplicationContext, io.opentracing.Tracer)} and {@link
 OpaRestTemplateConfiguration#OpaRestTemplateConfiguration(Duration, Duration)}.

 <p>Constraints provided with the Open Policy Agent response can be mapped to a custom pojo. If
 the class extends {@link AbstractConstraints} and is annotated with {@link Constraints} it can be
 {@link org.springframework.beans.factory.annotation.Autowired} in {@link
 org.springframework.web.servlet.mvc.Controller}s or {@link
 org.springframework.web.bind.annotation.RestController}s.

 <p>Additional parameters that are needed for the authorization decision may be provided with
 custom {@link OpaInputExtension}s.

 <p>Testing {@code SpringBootTest}s is supported by {@code DisableSdaAuth} and {@code
 EnableSdaAuthMock}.

 <p>{@code /openapi.yaml} and {@code /openapi.json} are excluded from authorization requirements.
 Custom excluded paths can be configured as comma separated list of regex in {@code
 opa.exclude.patterns}. This will overwrite the default excludes of the OpenAPI documentation
 paths.

 <p>Spring Security is disabled for the Management/Admin Port (default: 8081). Be aware that these
 port should not be accessible out of the deployment context.

 <p>This security implementation lacks some features compared to <a
 href="https://github.com/SDA-SE/sda-dropwizard-commons/tree/master/sda-commons-server-auth">sda-dropwizard-commons</a>:

 <ul>
   <li>No configuration of static local public keys to verify the token signature.
   <li>No configuration of JWKS URIs to verify the token signature.
   <li>The IDP must provide an {@code iss} claim that matches the base URI for discovery.
   <li>Leeway is not configurable yet.
   <li>The client that loads the JWKS is not configurable yet.</li>
</ul>