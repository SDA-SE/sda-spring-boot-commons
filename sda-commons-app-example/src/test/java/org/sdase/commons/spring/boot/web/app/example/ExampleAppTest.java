/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.ws.rs.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ContextConfiguration(initializers = {EnableSdaAuthMockInitializer.class})
@AutoConfigureTestRestTemplate
class ExampleAppTest {
  @LocalServerPort private int port;

  @Autowired AuthMock authMock;

  @Autowired TestRestTemplate restTemplate;

  @BeforeEach
  void reset() {
    authMock.reset();
  }

  @Test
  void shouldRejectAnonymousRequest() {
    authMock.authorizeRequest().withHttpMethod(HttpMethod.GET).withPath("/cars").deny();

    var response = restTemplate.getForEntity("http://localhost:" + port + "/api/cars", Car.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldGetCars() {
    authMock.authorizeAnyRequest().allow();

    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/cars", Cars.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getCars())
        .extracting("licensePlate")
        .containsExactly("HH-AB 1200", "LG-CD 2000");
  }

  @Test
  void shouldGetTrees() {
    authMock.authorizeAnyRequest().allow();

    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/trees", Trees.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotNull();
    assertThat(response.getBody().getTrees())
        .extracting("name")
        .containsExactly("Yellow Birch", "American Elm");
  }
}
