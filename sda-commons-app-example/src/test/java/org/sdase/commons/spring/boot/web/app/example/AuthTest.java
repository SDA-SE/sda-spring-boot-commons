/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

// ATTENTION: The source of this class is included in the public documentation.

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.HttpMethod;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "management.server.port=0")
@ContextConfiguration(initializers = {EnableSdaAuthMockInitializer.class})
class AuthTest {
  @LocalServerPort private int port;

  @Autowired AuthMock authMock;

  @BeforeEach
  void reset() {
    authMock.reset();
  }

  @Test
  void shouldRejectRequest() {
    authMock.authorizeRequest().withHttpMethod(HttpMethod.GET).withPath("/cars").deny();

    var response =
        authMock.authentication().authenticatedClient().getForEntity(carsApiUrl(), Cars.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldAllowWithConstraints() {
    authMock
        .authorizeRequest()
        .withHttpMethod(HttpMethod.GET)
        .withPath("/cars")
        .allowWithConstraint(Map.of("drivers", List.of("driver-123")));

    var response =
        authMock
            .authentication()
            .withSubject("driver-123")
            .authenticatedClient()
            .getForEntity(carsApiUrl(), Cars.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private String carsApiUrl() {
    return "http://localhost:%s/api/cars".formatted(port);
  }
}
