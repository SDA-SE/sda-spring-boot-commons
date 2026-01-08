/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sdase.commons.spring.boot.web.security.test.ContextUtils.createTestContext;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.security.test.SecurityTestApp;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authorization.AuthorizationDecision;

class ManagementAuthorizationManagerCoverageTest {

  @Test
  void shouldNotDecideOnSamePort() {
    assertThat(createTestContext(ActuatorSamePortApp.class))
        .hasSingleBean(
            ManagementAuthorizationManager.IgnoreSamePortManagementAccessDecisionVoter.class)
        .getBean(ManagementAuthorizationManager.IgnoreSamePortManagementAccessDecisionVoter.class)
        .returns(
            new AuthorizationDecision(false).isGranted(),
            dv -> dv.authorize(null, null).isGranted());
  }

  @Test
  void shouldNotDecideOnDisabledPort() {
    assertThat(createTestContext(ActuatorDisabledPortApp.class))
        .hasSingleBean(ManagementAuthorizationManager.DisabledManagementAccessDecisionVoter.class)
        .getBean(ManagementAuthorizationManager.DisabledManagementAccessDecisionVoter.class)
        .returns(
            new AuthorizationDecision(false).isGranted(),
            dv -> dv.authorize(null, null).isGranted());
  }

  @SpringBootApplication
  @SpringBootTest(
      classes = SecurityTestApp.class,
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = "management.server.port=")
  static class ActuatorSamePortApp {}

  @SpringBootApplication
  @SpringBootTest(
      classes = SecurityTestApp.class,
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
      properties = "management.server.port=-1")
  static class ActuatorDisabledPortApp {}
}
