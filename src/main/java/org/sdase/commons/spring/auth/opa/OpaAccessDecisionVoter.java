/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.auth.opa;

import static io.opentracing.tag.Tags.COMPONENT;
import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.Tracer;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import org.sdase.commons.spring.auth.opa.model.OpaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class OpaAccessDecisionVoter implements AccessDecisionVoter<FilterInvocation> {

  static final String CONSTRAINTS_ATTRIBUTE =
      OpaAccessDecisionVoter.class.getName() + ".constraints";

  private static final Logger LOG = LoggerFactory.getLogger(OpaAccessDecisionVoter.class);

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
   * @param tracer
   */
  public OpaAccessDecisionVoter(
      @Value("${opa.disable:false}") boolean disableOpa,
      @Value("${opa.base.url:http://localhost:8181}") String opaBaseUrl,
      @Value("${opa.policy.package:}") String policyPackage,
      OpaRequestBuilder opaRequestBuilder,
      @Qualifier("opaRestTemplate") RestTemplate opaRestTemplate,
      ApplicationContext applicationContext,
      Tracer tracer) {
    this.disableOpa = disableOpa;
    this.opaRestTemplate = opaRestTemplate;
    this.tracer = tracer;
    var derivedPolicyPackage = createOpaPackageName(policyPackage, applicationContext);
    this.opaRequestUrl = createOpaRequestUri(opaBaseUrl, derivedPolicyPackage);
    this.opaRequestBuilder = opaRequestBuilder;
    if (this.disableOpa) {
      LOG.warn("OPA is disabled. Access will be granted always.");
    }
  }

  @Override
  public boolean supports(ConfigAttribute attribute) {
    return true;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return FilterInvocation.class.isAssignableFrom(clazz);
  }

  @Override
  public int vote(
      Authentication authentication,
      FilterInvocation filterInvocation,
      Collection<ConfigAttribute> attributes) {
    var httpRequest = filterInvocation.getHttpRequest();
    Span span =
        tracer
            .buildSpan("authorizeUsingOpa")
            .withTag("opa.allow", false)
            .withTag(COMPONENT, "OpaAuthFilter")
            .start();
    try (Scope ignored = tracer.scopeManager().activate(span)) {
      if (disableOpa) {
        return handleOpaDisabled(httpRequest);
      }
      OpaResponse opaResponse = authorizeWithOpa(httpRequest);
      if (opaResponse == null) {
        LOG.warn(
            "Invalid response from OPA. Maybe the policy path or the response format is not correct");
        return ACCESS_ABSTAIN;
      }
      span.setTag("opa.allow", opaResponse.isAllow());
      if (!opaResponse.isAllow()) {
        return ACCESS_ABSTAIN;
      }
      storeConstraints(opaResponse);
      return ACCESS_GRANTED;
    } finally {
      span.finish();
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

  private int handleOpaDisabled(HttpServletRequest httpRequest) {
    if (httpRequest.getUserPrincipal() == null) {
      LOG.warn("OPA is disabled. Access is granted for anonymous user without constraints.");
    }
    return ACCESS_GRANTED;
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
