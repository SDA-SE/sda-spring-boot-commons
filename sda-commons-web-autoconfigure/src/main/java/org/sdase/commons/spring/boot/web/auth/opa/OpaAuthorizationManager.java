/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.opa;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.servlet.http.HttpServletRequest;
import java.util.function.Supplier;
import org.sdase.commons.spring.boot.web.auth.opa.model.OpaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class OpaAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

  static final String CONSTRAINTS_ATTRIBUTE =
      OpaAuthorizationManager.class.getName() + ".constraints";

  private static final Logger LOG = LoggerFactory.getLogger(OpaAuthorizationManager.class);

  private final boolean disableOpa;
  private final String opaRequestUrl;
  private final OpaRequestBuilder opaRequestBuilder;
  private final RestTemplate opaRestTemplate;

  private final Tracer tracer;

  /**
   * @param disableOpa Disables authorization checks with Open Policy Agent completely. In this case
   *     access to all resources is granted but no constraints are provided. Defaults to {@code
   *     false}.
   * @param opaBaseUrl The baseUrl of the Open Policy Agent Server. Defaults to {@code
   *     http://localhost:8181}.
   *     <p>Requests to the server are determined by the base URL and the policy package. Given the
   *     default base URL {@code http://localhost:8181} and an example package of {@code
   *     com.my.service}, the Open Policy Agent server will be requested for authorization decision
   *     at {@code http://localhost:8181/v1/data/com/my/package}
   * @param policyPackage The policy package to check for authorization. It will be reformatted to a
   *     URL path to request the authorization form the Open Policy Agent server. Example: {@code
   *     com.my.service}. If the policy package is blank, the package of the application class (the
   *     first bean found that is annotated with {@link SpringBootApplication}) is used as a
   *     default. Be aware that moving the class causes a breaking change regarding deployment if
   *     the package is not explicitly set.
   *     <p>Requests to the server are determined by the base URL and the policy package. Given the
   *     default base URL {@code http://localhost:8181} and an example package of {@code
   *     com.my.service}, the Open Policy Agent server will be requested for authorization decision
   *     at {@code http://localhost:8181/v1/data/com/my/package}
   * @param opaRequestBuilder a service to create the request payload that is sent to OPA
   * @param opaRestTemplate the {@code RestTemplate} used to make requests to the external Open
   *     Policy Agent server
   * @param applicationContext the current application context is used to derive the default OPA
   * @param openTelemetry the open telemetry instance used to send traces to an OTLP compatible
   *     collector
   */
  public OpaAuthorizationManager(
      @Value("${opa.disable:false}") boolean disableOpa,
      @Value("${opa.base.url:http://localhost:8181}") String opaBaseUrl,
      @Value("${opa.policy.package:}") String policyPackage,
      OpaRequestBuilder opaRequestBuilder,
      @Qualifier("opaRestTemplate") RestTemplate opaRestTemplate,
      ApplicationContext applicationContext,
      OpenTelemetry openTelemetry) {
    this.disableOpa = disableOpa;
    this.opaRestTemplate = opaRestTemplate;
    this.tracer = openTelemetry.getTracer("sda-commons-web-autoconfigure");
    var derivedPolicyPackage = createOpaPackageName(policyPackage, applicationContext);
    this.opaRequestUrl = createOpaRequestUri(opaBaseUrl, derivedPolicyPackage);
    this.opaRequestBuilder = opaRequestBuilder;
    if (this.disableOpa) {
      LOG.warn("OPA is disabled. Access will be granted always.");
    }
  }

  @Override
  public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
    AuthorizationManager.super.verify(authentication, object);
  }

  @Override
  public AuthorizationDecision check(
      Supplier<Authentication> authentication,
      RequestAuthorizationContext requestAuthorizationContext) {
    var httpRequest = requestAuthorizationContext.getRequest();
    Span span =
        tracer
            .spanBuilder("authorizeUsingOpa")
            .setAttribute("opa.allow", false)
            .setAttribute("component", "OpaAuthFilter")
            .startSpan();
    try (Scope ignored = span.makeCurrent()) {
      if (disableOpa) {
        return handleOpaDisabled(httpRequest);
      }
      OpaResponse opaResponse = authorizeWithOpa(httpRequest);
      if (opaResponse == null) {
        LOG.warn(
            "Invalid response from OPA. Maybe the policy path or the response format is not correct");
        return new AuthorizationDecision(false);
      }
      span.setAttribute("opa.allow", opaResponse.isAllow());
      if (!opaResponse.isAllow()) {
        return new AuthorizationDecision(false);
      }
      storeConstraints(opaResponse);
      return new AuthorizationDecision(true);
    } finally {
      span.end();
    }
  }

  private void storeConstraints(OpaResponse opaResponse) {
    try {
      RequestContextHolder.currentRequestAttributes()
          .setAttribute(CONSTRAINTS_ATTRIBUTE, opaResponse, SCOPE_REQUEST);
    } catch (NullPointerException | IllegalStateException ignored) {
      // ignored
    }
  }

  private OpaResponse authorizeWithOpa(HttpServletRequest httpRequest) {
    try {
      var opaRequestPayload = opaRequestBuilder.buildRequestPayload(httpRequest);
      return opaRestTemplate.postForObject(opaRequestUrl, opaRequestPayload, OpaResponse.class);
    } catch (ResourceAccessException e) {
      LOG.warn("Failed to access OPA", e);
      return null;
    }
  }

  OpaResponse getOpaForObject() {
    return opaRestTemplate.getForObject(opaRequestUrl, OpaResponse.class);
  }

  private AuthorizationDecision handleOpaDisabled(HttpServletRequest httpRequest) {
    if (httpRequest.getUserPrincipal() == null) {
      LOG.warn("OPA is disabled. Access is granted for anonymous user without constraints.");
    }
    return new AuthorizationDecision(true);
  }

  private String createOpaRequestUri(String opaBaseUrl, String policyPackage) {
    return String.format(
        "%s/v1/data/%s", opaBaseUrl.trim(), createPolicyPathFromPackage(policyPackage));
  }

  private String createPolicyPathFromPackage(String policyPackage) {
    final String[] pathSegmentsOfPolicyPackage = policyPackage.split("\\.");
    return String.join("/", pathSegmentsOfPolicyPackage).trim();
  }

  private String createOpaPackageName(
      String configuredOpaPackageName, ApplicationContext applicationContext) {
    if (!configuredOpaPackageName.isBlank()) {
      return configuredOpaPackageName;
    }
    var applicationsByBeanName =
        applicationContext.getBeansWithAnnotation(SpringBootApplication.class);
    return applicationsByBeanName.keySet().stream()
        .findFirst()
        .map(applicationsByBeanName::get)
        .map(Object::getClass)
        .map(Class::getPackageName)
        .orElse(configuredOpaPackageName);
  }
}
