/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth;

import java.util.function.Supplier;
import org.jspecify.annotations.Nullable;
import org.sdase.commons.spring.boot.web.auth.management.ManagementAuthorizationManager;
import org.sdase.commons.spring.boot.web.auth.opa.OpaAuthorizationManager;
import org.sdase.commons.spring.boot.web.auth.opa.OpaExcludesAuthorizationManager;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
public class SdaAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
  private final AuthorizationManager<RequestAuthorizationContext> authorizationManager;

  public SdaAuthorizationManager(
      ManagementAuthorizationManager managementAccessDecisionVoter,
      OpaExcludesAuthorizationManager opaExcludesDecisionVoter,
      OpaAuthorizationManager opaAccessDecisionVoter) {
    authorizationManager =
        AuthorizationManagers.anyOf(
            managementAccessDecisionVoter, opaExcludesDecisionVoter, opaAccessDecisionVoter);
  }

  @Override
  public void verify(
      Supplier<? extends @Nullable Authentication> authentication,
      RequestAuthorizationContext filterInvocation) {
    authorizationManager.verify(authentication, filterInvocation);
  }

  @Override
  public @Nullable AuthorizationResult authorize(
      Supplier<? extends @Nullable Authentication> authentication,
      RequestAuthorizationContext filterInvocation) {
    return authorizationManager.authorize(authentication, filterInvocation);
  }
}
