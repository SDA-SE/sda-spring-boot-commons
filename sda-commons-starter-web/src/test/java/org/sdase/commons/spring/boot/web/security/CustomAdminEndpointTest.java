/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sdase.commons.spring.boot.web.security.test.SecurityTestApp;
import org.sdase.commons.spring.boot.web.security.test.TestResource;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = SecurityTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
@AutoConfigureTestRestTemplate
class CustomAdminEndpointTest {

  @LocalServerPort private int port;

  @LocalManagementPort private int managementPort;

  @Autowired private TestRestTemplate client;

  @Autowired private AuthMock authMock;

  @Test
  void shouldAccessCustomEndpointAtManagementPort() {
    authMock.authorizeAnyRequest().deny();
    ResponseEntity<TestResource> actual =
        client.getForEntity(
            String.format("http://localhost:%s", managementPort) + "/tasks/doSomething",
            TestResource.class);
    assertThat(actual).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.OK);
    assertThat(actual)
        .extracting(ResponseEntity::getBody)
        .extracting(TestResource::getValue)
        .isEqualTo("This is from the management server.");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "/api/tasks/doSomething",
        "/api/doSomething",
        "/actuator/tasks/doSomething",
        "/actuator/doSomething",
        "/api/actuator/tasks/doSomething",
        "/api/actuator/doSomething",
        "/tasks/doSomething",
        "/doSomething"
      })
  void shouldNotAccessCustomEndpointAtAppPort(String pathToTry) {
    authMock.authorizeAnyRequest().allow();
    ResponseEntity<String> actual =
        client.getForEntity(String.format("http://localhost:%s", port) + pathToTry, String.class);
    assertThat(actual).extracting(ResponseEntity::getStatusCode).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
