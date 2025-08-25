/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth.opa;

import java.util.function.Supplier;
import org.sdase.commons.spring.boot.mcp.server.auth.opa.model.OpaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenPolicyAgentAuthorizationManager
    implements AuthorizationManager<RequestAuthorizationContext> {

  private static final Logger LOG =
      LoggerFactory.getLogger(OpenPolicyAgentAuthorizationManager.class);

  private final OpaRequestBuilder opaRequestBuilder;

  private final RestTemplate opaRestTemplate;

  private final Boolean disableOpa;

  private final String opaRequestUrl;

  OpenPolicyAgentAuthorizationManager(
      @Value("${opa.disable:false}") boolean disableOpa,
      @Value("${opa.base.url:http://localhost:8181}") String opaBaseUrl,
      @Value("${opa.policy.package:}") String policyPackage,
      OpaRequestBuilder opaRequestBuilder,
      RestTemplate opaRestTemplate) {
    this.opaRequestBuilder = opaRequestBuilder;
    this.opaRestTemplate = opaRestTemplate;
    this.disableOpa = disableOpa;
    this.opaRequestUrl = buildOpaUrl(opaBaseUrl, policyPackage);
    if (Boolean.TRUE.equals(this.disableOpa)) {
      LOG.warn("OPA is disabled. Access will be granted always.");
    }
  }

  @Override
  public AuthorizationDecision check(
      Supplier<Authentication> authentication, RequestAuthorizationContext object) {
    final var authResult = authorize(authentication, object);
    if (authResult != null) {
      return new AuthorizationDecision(authResult.isGranted());
    } else {
      return new AuthorizationDecision(false);
    }
  }

  @Override
  public AuthorizationResult authorize(
      Supplier<Authentication> authentication, RequestAuthorizationContext context) {

    if (Boolean.TRUE.equals(disableOpa)) {
      if (context.getRequest().getUserPrincipal() == null) {
        LOG.warn("OPA is disabled. Access is granted for anonymous user without constraints.");
      }
      return new AuthorizationDecision(true);
    }

    try {
      OpaResponse result =
          opaRestTemplate.postForObject(
              opaRequestUrl,
              opaRequestBuilder.buildRequestPayload(context.getRequest()),
              OpaResponse.class);

      if (result == null) {
        LOG.warn("OPA response is null, denying access.");
        return new AuthorizationDecision(false);
      }
      return new AuthorizationDecision(result.isAllow());
    } catch (RestClientException e) {
      LOG.warn("Error while calling OPA at {}", opaRequestUrl, e);
      return new AuthorizationDecision(false);
    }
  }

  private String buildOpaUrl(String opaBaseUrl, String policyPackage) {
    return String.format("%s/%s/%s", opaBaseUrl, "v1/data", convertToPath(policyPackage));
  }

  private String convertToPath(String policyPackage) {
    return policyPackage.replace('.', '/');
  }
}
