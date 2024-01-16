/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.auth.management.ManagementAuthorizationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;

@SpringBootTest(
    classes = App.class,
    webEnvironment = RANDOM_PORT,
    properties = "management.server.port=8888")
class ManagementAuthorizationManagerWithModifiedManagementPortTest {

  @Autowired private ManagementAuthorizationManager managementAccessDecisionVoter;

  @LocalManagementPort private int managementPort;

  @Test
  void beanShouldBeAvailable() {
    assertThat(managementAccessDecisionVoter).isNotNull();
  }

  @Test
  void managementPortShouldBeSetToDefault() {
    assertThat(managementPort).isEqualTo(8888);
  }
}
