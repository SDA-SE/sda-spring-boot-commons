/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.handler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.error.ApiError;
import org.sdase.commons.spring.boot.web.security.test.SecurityTestApp;
import org.sdase.commons.spring.boot.web.security.test.TestResource;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = SecurityTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
class ObscuringErrorHandlerTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @Test
  void shouldTransformToStandardErrors() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<ApiError> exchange =
        client.exchange(
            getServerBaseUrl() + "/api/errorEntity",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ApiError.class);
    assertThat(exchange)
        .isNotNull()
        .extracting(ResponseEntity::getBody)
        .extracting(ApiError::getTitle)
        .isEqualTo("HTTP Error 500 occurred.");
  }

  @Test
  void shouldNotTransformApiErrors() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<ApiError> exchange =
        client.exchange(
            getServerBaseUrl() + "/api/apiError",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ApiError.class);
    assertThat(exchange)
        .isNotNull()
        .extracting(ResponseEntity::getStatusCode, r -> r.getBody().getTitle())
        .containsExactly(HttpStatus.NOT_IMPLEMENTED, "This method is not implemented yet.");
  }

  @Test
  void shouldMapExceptions() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<ApiError> exchange =
        client.exchange(
            getServerBaseUrl() + "/api/forcedError",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ApiError.class);
    assertThat(exchange)
        .isNotNull()
        .extracting(ResponseEntity::getStatusCode, r -> r.getBody().getTitle())
        .containsExactly(HttpStatus.INTERNAL_SERVER_ERROR, "An exception occurred.");
  }

  @Test
  void shouldGetResponseEntityWithBody() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<TestResource> exchange =
        client.exchange(
            getServerBaseUrl() + "/api/response",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            TestResource.class);
    assertThat(exchange)
        .isNotNull()
        .extracting(ResponseEntity::getStatusCode, r -> r.getBody().getValue())
        .containsExactly(HttpStatus.OK, "This will not be altered.");
  }

  @Test
  void shouldGetErrorResponseFromVoidReturnType() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<ApiError> exchange =
        client.exchange(
            getServerBaseUrl() + "/api/voidError",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            ApiError.class);
    assertThat(exchange)
        .isNotNull()
        .extracting(ResponseEntity::getStatusCode, r -> r.getBody().getTitle())
        .containsExactly(HttpStatus.NOT_FOUND, "HTTP Error 404 occurred.");
  }

  @Test
  void shouldGetResource() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<TestResource> exchange =
        client.exchange(
            getServerBaseUrl() + "/api/resource",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            TestResource.class);
    assertThat(exchange)
        .isNotNull()
        .extracting(ResponseEntity::getStatusCode, r -> r.getBody().getValue())
        .containsExactly(HttpStatus.OK, "This will not be altered.");
  }

  @Test
  void shouldGetApiErrorOnNotNullAnnotation() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var exchange =
        client.postForEntity(
            getServerBaseUrl() + "/api/validate",
            new TestResource().setPostcode("1234"),
            ApiError.class);
    assertThat(exchange)
        .isNotNull()
        .extracting(ResponseEntity::getStatusCode, r -> r.getBody().getTitle())
        .containsExactly(HttpStatus.UNPROCESSABLE_ENTITY, "Validation error");
  }

  @Test
  void shouldGetApiErrorOnCustomValidatorAnnotation() {
    var headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    var exchange =
        client.postForEntity(
            getServerBaseUrl() + "/api/validate",
            new TestResource().setValue("test").setPostcode("ab123"),
            ApiError.class);
    assertThat(exchange)
        .isNotNull()
        .extracting(ResponseEntity::getStatusCode, r -> r.getBody().getTitle())
        .containsExactly(HttpStatus.UNPROCESSABLE_ENTITY, "Validation error");
  }

  String getServerBaseUrl() {
    return String.format("http://localhost:%s", port);
  }
}
