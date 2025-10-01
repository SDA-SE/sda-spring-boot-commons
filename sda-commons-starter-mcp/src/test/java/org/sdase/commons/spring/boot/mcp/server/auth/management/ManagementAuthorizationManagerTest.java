/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.mcp.server.auth.management.ManagementAuthorizationManager.DifferentPortManagementAccessDecisionVoter;
import org.sdase.commons.spring.boot.mcp.server.auth.management.ManagementAuthorizationManager.DisabledManagementAccessDecisionVoter;
import org.sdase.commons.spring.boot.mcp.server.auth.management.ManagementAuthorizationManager.IgnoreSamePortManagementAccessDecisionVoter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

class ManagementAuthorizationManagerTest {

  private HttpServletRequest request;
  private RequestAuthorizationContext context;
  private Supplier<Authentication> authentication;

  @BeforeEach
  void setUp() {
    request = mock(HttpServletRequest.class);
    context = mock(RequestAuthorizationContext.class);
    authentication = mock(Supplier.class);
    when(context.getRequest()).thenReturn(request);
  }

  @Test
  void shouldGrantAccessForDifferentPortWhenRequestComesFromManagementPort() throws Exception {
    // Given
    var manager = new DifferentPortManagementAccessDecisionVoter();
    when(request.getLocalPort()).thenReturn(8081);

    // Set management port via reflection (simulating event handling)
    setManagementPort(manager, 8081);

    // When
    var decision = manager.check(authentication, context);

    // Then
    assertThat(decision.isGranted()).isTrue();
  }

  @Test
  void shouldDenyAccessForDifferentPortWhenRequestComesFromApplicationPort() throws Exception {
    // Given
    var manager = new DifferentPortManagementAccessDecisionVoter();
    when(request.getLocalPort()).thenReturn(8080);

    // Set management port via reflection (simulating event handling)
    setManagementPort(manager, 8081);

    // When
    var decision = manager.check(authentication, context);

    // Then
    assertThat(decision.isGranted()).isFalse();
  }

  @Test
  void shouldDenyAccessForDifferentPortWhenNoManagementPortInitialized() {
    // Given
    var manager = new DifferentPortManagementAccessDecisionVoter();
    when(request.getLocalPort()).thenReturn(8080);

    // When (no management port set, defaults to -1)
    var decision = manager.check(authentication, context);

    // Then
    assertThat(decision.isGranted()).isFalse();
  }

  @Test
  void shouldDenyAccessForSamePortConfiguration() {
    // Given
    var manager = new IgnoreSamePortManagementAccessDecisionVoter();

    // When
    var decision = manager.check(authentication, context);

    // Then
    assertThat(decision.isGranted()).isFalse();
  }

  @Test
  void shouldDenyAccessForDisabledManagementConfiguration() {
    // Given
    var manager = new DisabledManagementAccessDecisionVoter();

    // When
    var decision = manager.check(authentication, context);

    // Then
    assertThat(decision.isGranted()).isFalse();
  }

  private void setManagementPort(DifferentPortManagementAccessDecisionVoter manager, int port)
      throws Exception {
    Field field =
        DifferentPortManagementAccessDecisionVoter.class.getDeclaredField("managementPort");
    field.setAccessible(true);
    field.set(manager, port);
  }
}
