/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

import static org.assertj.core.api.Assertions.assertThat;

import javax.ws.rs.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = {EnableSdaAuthMockInitializer.class})
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
    authMock.authorizeRequest().withHttpMethod(HttpMethod.GET).withPath("/myResource").deny();

    var response =
        restTemplate.getForEntity("http://localhost:" + port + "/api/myResource", Car.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldGetStacks() {
    authMock.authorizeAnyRequest().allow();

    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/myResource", Object.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).extracting("value").isEqualTo("the value");
  }
}
