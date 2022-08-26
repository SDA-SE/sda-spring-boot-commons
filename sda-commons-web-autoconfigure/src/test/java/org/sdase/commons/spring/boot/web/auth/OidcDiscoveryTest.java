/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.github.tomakehurst.wiremock.client.WireMock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = AuthTestApp.class,
    webEnvironment = RANDOM_PORT,
    properties = {"management.server.port=0"})
@ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
class OidcDiscoveryTest {

  @Autowired private AuthMock authMock;

  @LocalServerPort private int port;

  @BeforeEach
  void resetAuthMock() {
    authMock.reset();
    authMock.authorizeAnyRequest().allow();
  }

  @Test
  void shouldCacheJwksForSubsequentRequestsWithSameKid() {
    authMock.wireMockServer().resetRequests();
    var initialResponse =
        authMock
            .authentication()
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    assertThat(initialResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(initialResponse.getBody()).extracting("authenticated").isEqualTo(true);
    for (int i = 0; i < 10; i++) {
      var subsequentResponse =
          authMock
              .authentication()
              .authenticatedClient()
              .getForEntity("http://localhost:" + port + "/api/ping", Object.class);
      assertThat(subsequentResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(subsequentResponse.getBody()).extracting("authenticated").isEqualTo(true);
    }
    authMock
        .wireMockServer()
        .verify(1, WireMock.getRequestedFor(WireMock.urlPathEqualTo("/issuer/keys")));
  }

  @Test
  void shouldReloadJwksForNewKid() {
    var responseBeforeRotation =
        authMock
            .authentication()
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    authMock.rotateKey();
    var responseAfterRotation =
        authMock
            .authentication()
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    assertThat(responseBeforeRotation.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseBeforeRotation.getBody()).extracting("authenticated").isEqualTo(true);
    assertThat(responseAfterRotation.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(responseAfterRotation.getBody()).extracting("authenticated").isEqualTo(true);
  }

  @Test
  @Disabled(
      "Currently there is no way to configure or trigger the cache invalidation. "
          + "The cache expires after 15m and is refreshed after 5m, "
          + "see DefaultJWKSetCache#DefaultJWKSetCache()")
  void shouldRejectOldKey() {
    var client = authMock.authentication().authenticatedClient();
    var beforeRotation =
        client.getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    assertThat(beforeRotation.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(beforeRotation.getBody()).extracting("authenticated").isEqualTo(true);
    authMock.rotateKey();
    var afterRotation = client.getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    assertThat(afterRotation.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }
}
