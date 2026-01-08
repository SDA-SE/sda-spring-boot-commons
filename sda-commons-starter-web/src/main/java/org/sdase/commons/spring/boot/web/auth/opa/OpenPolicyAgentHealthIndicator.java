/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.opa;

import org.sdase.commons.spring.boot.web.auth.opa.model.OpaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.OperationResponseBody;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.health.contributor.AbstractHealthIndicator;
import org.springframework.boot.health.contributor.Health;
import org.springframework.stereotype.Component;

/**
 * Calls the Open Policy Agent sidecar health check, if enabled, and sets the health status based on
 * the response. You can disable this check by setting the property {@systemProperty opa.disable} to
 * true
 */
@Component
@ConditionalOnProperty(name = "opa.disable", havingValue = "false", matchIfMissing = true)
public class OpenPolicyAgentHealthIndicator extends AbstractHealthIndicator
    implements OperationResponseBody {

  private static final Logger LOG = LoggerFactory.getLogger(OpenPolicyAgentHealthIndicator.class);

  private final OpaAuthorizationManager opaAccessDecisionVoter;

  public OpenPolicyAgentHealthIndicator(OpaAuthorizationManager opaAccessDecisionVoter) {
    this.opaAccessDecisionVoter = opaAccessDecisionVoter;
  }

  /**
   * Sets the health check status based on Open Policy Agent response. If the response is null or if
   * it is allowed, it sets the health status to {@literal DOWN} If the response is denying, it sets
   * the health status to {@literal UP}
   *
   * @param builder the {@link Health.Builder} to report health status and details
   */
  @Override
  protected void doHealthCheck(Health.Builder builder) {
    try {
      // send a get request to the policy path. The get will not provide any input.
      // Normally, the policy should respond with a denying decision.
      // If there is an exception, the check will be unhealthy
      OpaResponse opaResponse = opaAccessDecisionVoter.getOpaForObject();

      if (opaResponse == null
          || opaResponse.getResult() == null
          || opaResponse.getResult().isNull()) {
        LOG.warn("The policy response seems not to be SDA guideline compliant");
        unhealthy(builder, "The policy response seems not to be SDA guideline compliant");
        return;
      }

      if (opaResponse.isAllow()) {
        LOG.warn("The policy should respond with a deny decision by default");
        unhealthy(builder, "The policy should respond with a deny decision by default");
        return;
      }

      builder.up().withDetail("healthy", true);
    } catch (Exception e) {
      LOG.warn("Failed health check", e);
      unhealthy(builder, e.getMessage());
    }
  }

  private static void unhealthy(Health.Builder builder, String message) {
    builder.down().withDetail("healthy", false).withDetail("message", message);
  }
}
