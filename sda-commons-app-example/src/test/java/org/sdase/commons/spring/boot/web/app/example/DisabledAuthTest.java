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

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "management.server.port=0")
@ContextConfiguration(initializers = {DisableSdaAuthInitializer.class})
@AutoConfigureTestRestTemplate
class DisabledAuthTest {
  @LocalServerPort private int port;

  @Autowired TestRestTemplate restTemplate;

  @Test
  void shouldGetResourceWithoutAuthentication() {
    var response = restTemplate.getForEntity(carsApiUrl(), Cars.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private String carsApiUrl() {
    return "http://localhost:%s/api/cars".formatted(port);
  }
}
