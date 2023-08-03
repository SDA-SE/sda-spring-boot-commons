/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.management;

import java.util.function.Supplier;
import org.springframework.boot.actuate.autoconfigure.web.server.ConditionalOnManagementPort;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

public interface ManagementAuthorizationManager
    extends AuthorizationManager<RequestAuthorizationContext> {

  @Override
  default void verify(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
    AuthorizationManager.super.verify(authentication, object);
  }

  @Component
  @ConditionalOnManagementPort(ManagementPortType.DIFFERENT)
  class DifferentPortManagementAccessDecisionVoter implements ManagementAuthorizationManager {

    /**
     * The management port discovered in {@link
     * #onApplicationEvent(ServletWebServerInitializedEvent)}. Initially a value that can't be an
     * existing port to avoid granting access by accident to the application API.
     */
    private int managementPort = -1;

    @EventListener
    public void onApplicationEvent(ServletWebServerInitializedEvent event) {
      if ("management".equals(event.getApplicationContext().getServerNamespace())) {
        this.managementPort = event.getWebServer().getPort();
      }
    }

    @Override
    public AuthorizationDecision check(
        Supplier<Authentication> authentication, RequestAuthorizationContext object) {
      int requestLocalPort = object.getRequest().getLocalPort();
      return requestLocalPort == this.managementPort
          ? new AuthorizationDecision(true)
          : new AuthorizationDecision(false);
    }
  }

  @Component
  @ConditionalOnManagementPort(ManagementPortType.SAME)
  class IgnoreSamePortManagementAccessDecisionVoter implements ManagementAuthorizationManager {

    @Override
    public AuthorizationDecision check(
        Supplier<Authentication> authentication, RequestAuthorizationContext object) {
      return new AuthorizationDecision(false);
    }
  }

  @Component
  @ConditionalOnManagementPort(ManagementPortType.DISABLED)
  class DisabledManagementAccessDecisionVoter implements ManagementAuthorizationManager {

    @Override
    public AuthorizationDecision check(
        Supplier<Authentication> authentication, RequestAuthorizationContext object) {
      return new AuthorizationDecision(false);
    }
  }
}
