/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.handler;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.security.handler.app.ValidationExceptionBook;
import org.sdase.commons.spring.boot.web.security.handler.app.ValidationExceptionHandlerTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = ValidationExceptionHandlerTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"security.validation-exception-handler-enabled=false"})
@ContextConfiguration(initializers = {EnableSdaAuthMockInitializer.class})
class ValidationExceptionHandlerMissingTest {

  @LocalServerPort private int port;

  @Autowired private AuthMock authMock;

  @Test
  void shouldReturn200IfObjectIsValid() {

    authMock.authorizeAnyRequest().allow();

    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .postForEntity(
                "http://localhost:" + port + "/api/bookSpring",
                new ValidationExceptionBook("title", "author"),
                ValidationExceptionBook.class);

    ValidationExceptionBook body = response.getBody();
    assertThat(body).isNotNull();
    assertThat(body.title()).isEqualTo("title");
    assertThat(body.author()).isEqualTo("author");
  }

  @Test
  void testValidationThrowingMethodArgumentNotValidException() {

    authMock.authorizeAnyRequest().allow();

    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .postForEntity(
                "http://localhost:" + port + "/api/bookSpring",
                new ValidationExceptionBook("title", null),
                ValidationExceptionBook.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void testValidationThrowingHandlerMethodValidationException() {

    authMock.authorizeAnyRequest().allow();

    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .postForEntity(
                "http://localhost:" + port + "/api/bookJakarta",
                new ValidationExceptionBook("title", null),
                String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void testValidationThrowingHttpMessageNotReadableException() {

    authMock.authorizeAnyRequest().allow();

    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .postForEntity("http://localhost:" + port + "/api/bookJakarta", null, String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void testStringAsRequestBody() {

    authMock.authorizeAnyRequest().allow();

    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .postForEntity("http://localhost:" + port + "/api/bookString", "ab", String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }
}
