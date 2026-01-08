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

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;

@SpringBootTest(
    classes = AuthTestApp.class,
    webEnvironment = RANDOM_PORT,
    // verify authentication in this test, not authorization
    properties = {"opa.disable=true"})
@AutoConfigureTestRestTemplate
class AuthenticationNoConfigIT {

  @LocalServerPort private int port;

  @LocalManagementPort private int managementPort;

  @Autowired private TestRestTemplate client;

  @Test
  void shouldNotAllowUnknownJwt() {
    var unknownJwt =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9."
            + "eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ."
            + "SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    var headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", unknownJwt));

    var responseEntity =
        client.exchange(
            "http://localhost:" + port + "/api/ping",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Object.class);

    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldAllowHealthCheckWithoutAuthenticationOnManagementPort() {
    var responseEntity =
        client.getForEntity(
            String.format("http://localhost:%d/healthcheck", managementPort), Object.class);
    assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
  }

  @Test
  void shouldAllowMetricsWithoutAuthenticationOnManagementPort() {
    var responseEntity =
        client.getForEntity(
            String.format("http://localhost:%d/metrics", managementPort), Object.class);
    assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
  }

  @Test
  void shouldAllowAnonymous() {
    var responseEntity =
        client.getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
