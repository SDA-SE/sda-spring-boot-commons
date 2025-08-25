/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.auth.opa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Health indicator for Open Policy Agent connectivity. This is a placeholder implementation. */
@Component
public class OpenPolicyAgentHealthIndicator {

  private static final Logger LOG = LoggerFactory.getLogger(OpenPolicyAgentHealthIndicator.class);

  private final boolean disableOpa;
  private final OpaAuthorizationManager opaAuthorizationManager;

  public OpenPolicyAgentHealthIndicator(
      @Value("${opa.disable:false}") boolean disableOpa,
      OpaAuthorizationManager opaAuthorizationManager) {
    this.disableOpa = disableOpa;
    this.opaAuthorizationManager = opaAuthorizationManager;
  }

  /**
   * Checks OPA connectivity status.
   *
   * @return true if OPA is available or disabled, false otherwise
   */
  public boolean isOpaHealthy() {
    if (disableOpa) {
      return true;
    }

    try {
      var response = opaAuthorizationManager.getOpaForObject();
      return response != null;
    } catch (Exception e) {
      LOG.debug("OPA health check failed", e);
      return false;
    }
  }
}
