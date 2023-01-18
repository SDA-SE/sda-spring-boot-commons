/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.opa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.auth.AuthTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = AuthTestApp.class,
    webEnvironment = RANDOM_PORT,
    properties = {"opa.exclude.patterns=/ping,/ping/.*"})
@ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
class OpaExcludesDecisionVoterIT {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @Autowired private AuthMock authMock;

  @BeforeEach
  void denyAll() {
    authMock.reset();
    authMock.authorizeAnyRequest().deny();
  }

  @Test
  void shouldNotAllowOpenApiAnyMore() {
    var responseEntity =
        client.getForEntity(
            String.format("http://localhost:%d/api/openapi.yaml", port), Object.class);

    assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isFalse();
  }

  @Test
  void shouldAllowPingWithoutAuthentication() {
    var responseEntity =
        client.getForEntity(String.format("http://localhost:%d/api/ping", port), Object.class);

    assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
  }

  @Test
  void shouldAllowPingDetailWithoutAuthentication() {
    var responseEntity =
        client.getForEntity(
            String.format("http://localhost:%d/api/ping/hello", port), Object.class);

    assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
  }
}
