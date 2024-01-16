/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.management;

import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.core.env.Environment;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
public class ManagementAuthorizationManager
    implements AuthorizationManager<RequestAuthorizationContext> {

  private final Integer managementServerPort;

  private final Environment environment;

  public ManagementAuthorizationManager(
      Environment environment, @Value("${management.server.port}") Integer managementServerPort) {
    this.environment = environment;
    this.managementServerPort = managementServerPort;
  }

  @Override
  public void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
    AuthorizationManager.super.verify(authentication, object);
  }

  @Override
  public AuthorizationDecision check(
      Supplier<Authentication> authentication, RequestAuthorizationContext context) {
    return switch (ManagementPortType.get(environment)) {
      case SAME -> checkIfPortsAreSame();
      case DIFFERENT -> checkIfPortsAreDifferent(context);
      case DISABLED -> checkIfManagementServerPortIsDisabled();
    };
  }

  private AuthorizationDecision checkIfPortsAreDifferent(RequestAuthorizationContext context) {
    int requestLocalPort = context.getRequest().getLocalPort();
    return requestLocalPort == this.managementServerPort
        ? new AuthorizationDecision(true)
        : new AuthorizationDecision(false);
  }

  private AuthorizationDecision checkIfManagementServerPortIsDisabled() {
    return new AuthorizationDecision(false);
  }

  private AuthorizationDecision checkIfPortsAreSame() {
    return new AuthorizationDecision(false);
  }
}
