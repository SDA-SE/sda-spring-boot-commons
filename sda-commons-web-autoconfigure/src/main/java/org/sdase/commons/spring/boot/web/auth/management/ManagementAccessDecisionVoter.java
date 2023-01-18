/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.management;

import java.util.Collection;
import org.springframework.boot.actuate.autoconfigure.web.server.ConditionalOnManagementPort;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementPortType;
import org.springframework.boot.web.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

public interface ManagementAccessDecisionVoter extends AccessDecisionVoter<FilterInvocation> {

  @Override
  default boolean supports(ConfigAttribute attribute) {
    return true;
  }

  @Override
  default boolean supports(Class<?> clazz) {
    return FilterInvocation.class.isAssignableFrom(clazz);
  }

  @Component
  @ConditionalOnManagementPort(ManagementPortType.DIFFERENT)
  class DifferentPortManagementAccessDecisionVoter implements ManagementAccessDecisionVoter {

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
    public int vote(
        Authentication authentication,
        FilterInvocation filterInvocation,
        Collection<ConfigAttribute> attributes) {
      int requestLocalPort = filterInvocation.getRequest().getLocalPort();
      return requestLocalPort == this.managementPort ? ACCESS_GRANTED : ACCESS_ABSTAIN;
    }
  }

  @Component
  @ConditionalOnManagementPort(ManagementPortType.SAME)
  class IgnoreSamePortManagementAccessDecisionVoter implements ManagementAccessDecisionVoter {
    @Override
    public int vote(
        Authentication authentication,
        FilterInvocation filterInvocation,
        Collection<ConfigAttribute> attributes) {
      return ACCESS_ABSTAIN;
    }
  }

  @Component
  @ConditionalOnManagementPort(ManagementPortType.DISABLED)
  class DisabledManagementAccessDecisionVoter implements ManagementAccessDecisionVoter {
    @Override
    public int vote(
        Authentication authentication,
        FilterInvocation filterInvocation,
        Collection<ConfigAttribute> attributes) {
      return ACCESS_ABSTAIN;
    }
  }
}
