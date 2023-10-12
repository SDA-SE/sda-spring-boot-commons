/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.opa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.of;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
class OpaExcludesAuthorizationManagerIT {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @Autowired private AuthMock authMock;

  @BeforeEach
  void denyAll() {
    authMock.reset();
    authMock.authorizeAnyRequest().deny();
  }

  static Stream<Arguments> shouldAllowWithoutAuthentication() {
    return Stream.of(
        // custom
        of("http://localhost:%d/api/ping", Object.class),
        of("http://localhost:%d/api/ping/hello", Object.class),

        // should always exclude
        of("http://localhost:%d/api/openapi", Object.class),
        of("http://localhost:%d/api/openapi.yaml", String.class));
  }

  @ParameterizedTest
  @MethodSource
  void shouldAllowWithoutAuthentication(String path, Class responseType) {
    var responseEntity = client.getForEntity(String.format(path, port), responseType);

    assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
  }
}
