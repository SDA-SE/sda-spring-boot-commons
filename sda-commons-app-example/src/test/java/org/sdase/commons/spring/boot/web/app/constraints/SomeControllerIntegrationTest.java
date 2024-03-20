/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.constraints;

// ATTENTION: The source of this class is included in the public documentation.

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.app.constraints.test.App;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = App.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "management.server.port=0")
@ContextConfiguration(initializers = {EnableSdaAuthMockInitializer.class})
class SomeControllerIntegrationTest {

  @LocalServerPort private int port;

  @Autowired AuthMock authMock;

  @Test
  void shouldBeAdmin() {
    authMock.authorizeAnyRequest().allowWithConstraint(Map.of("admin", true));
    var actual =
        authMock.authentication().authenticatedClient().getForEntity(serviceUrl(), String.class);
    assertThat(actual.getBody()).isEqualTo("admin");
  }

  @Test
  void shouldBeUser() {
    authMock.authorizeAnyRequest().allowWithConstraint(Map.of("admin", false));
    var actual =
        authMock.authentication().authenticatedClient().getForEntity(serviceUrl(), String.class);
    assertThat(actual.getBody()).isEqualTo("user");
  }

  private String serviceUrl() {
    return "http://localhost:%s/api/me/category".formatted(port);
  }
}
