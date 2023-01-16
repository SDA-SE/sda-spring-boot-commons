/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.headers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sdase.commons.spring.boot.web.security.test.SecurityTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = SecurityTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"management.server.port=0"})
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
class RequestHeadersTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @ParameterizedTest
  @ValueSource(strings = {"/api/caller", "/api/does/not/exist"})
  void shouldRejectLargeInputHeadersOverEightKib(String path) {
    HttpHeaders requestHeaders = tooLargeRequestHeaders();

    ResponseEntity<String> actual =
        client.exchange(
            String.format("http://localhost:%s", port) + path,
            HttpMethod.GET,
            new HttpEntity<>(requestHeaders),
            String.class);

    assertThat(actual)
        .extracting(ResponseEntity::getStatusCode)
        .isEqualTo(HttpStatus.BAD_REQUEST); // Unfortunately not 431
  }

  @Disabled(
      "Setting a custom error handler does not affect the deep internals of Spring Boot and it's servlet implementations.")
  @ParameterizedTest
  @ValueSource(strings = {"/api/caller", "/api/does/not/exist"})
  void shouldNotProduceSpringIdentifiableErrorMessage(String path) {
    HttpHeaders requestHeaders = tooLargeRequestHeaders();

    ResponseEntity<String> actual =
        client.exchange(
            String.format("http://localhost:%s", port) + path,
            HttpMethod.GET,
            new HttpEntity<>(requestHeaders),
            String.class);

    assertThat(actual)
        .extracting(ResponseEntity::getBody)
        .asString()
        .doesNotContain("background-color", "#525D76");
  }

  private static HttpHeaders tooLargeRequestHeaders() {
    String chars = "0987654321abcdefghijklmnopqrstuvwxyz";
    StringBuilder valueMoreThanOneKib = new StringBuilder();
    while (valueMoreThanOneKib.length() < 1024) {
      valueMoreThanOneKib.append(chars);
    }

    HttpHeaders requestHeaders = new HttpHeaders();
    requestHeaders.add("X-Header-One", valueMoreThanOneKib.toString());
    requestHeaders.add("X-Header-Two", valueMoreThanOneKib.toString());
    requestHeaders.add("X-Header-Three", valueMoreThanOneKib.toString());
    requestHeaders.add("X-Header-Four", valueMoreThanOneKib.toString());
    requestHeaders.add("X-Header-Five", valueMoreThanOneKib.toString());
    requestHeaders.add("X-Header-Six", valueMoreThanOneKib.toString());
    requestHeaders.add("X-Header-Seven", valueMoreThanOneKib.toString());
    requestHeaders.add("X-Header-Eight", valueMoreThanOneKib.toString());
    return requestHeaders;
  }
}
