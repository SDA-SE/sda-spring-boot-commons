/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.headers;

import static java.util.Arrays.asList;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = SecurityTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
abstract class AbstractSecurityHeadersTest {
  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  static Stream<Arguments> predefinedRestfulApiSecurityHeaders() {
    return Stream.of(
        // cache headers
        of("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"),
        of("Pragma", "no-cache"),
        of("Expires", "0"),

        // security headers
        of("X-Frame-Options", "DENY"),
        of("X-Content-Type-Options", "nosniff"),
        of("X-XSS-Protection", "0"),
        of("Referrer-Policy", "same-origin"),
        of("X-Permitted-Cross-Domain-Policies", "none"),
        of("Content-Security-Policy", "default-src 'none'; frame-ancestors 'none'; sandbox"));
  }

  static Stream<Arguments> predefinedFrontendSecurityHeaders() {
    return Stream.of(
        // cache headers
        of("Cache-Control", "no-cache, no-store, max-age=0, must-revalidate"),
        of("Pragma", "no-cache"),
        of("Expires", "0"),

        // security headers
        of("X-Frame-Options", "DENY"),
        of("X-Content-Type-Options", "nosniff"),
        of("X-XSS-Protection", "0"),
        of("Referrer-Policy", "same-origin"),
        of("X-Permitted-Cross-Domain-Policies", "none"),
        of(
            "Content-Security-Policy",
            String.join(
                "; ",
                asList(
                    "default-src 'self'",
                    "script-src 'self'",
                    "img-src 'self'",
                    "style-src 'self'",
                    "font-src 'self'",
                    "frame-src 'none'",
                    "object-src 'none'"))));
  }

  protected abstract Stream<Arguments> predefinedSecurityHeaders();

  @ParameterizedTest
  @MethodSource("predefinedRestfulApiSecurityHeaders")
  void shouldAddSecurityHeaders(String predefinedHeaderName, String expectedPredefinedHeaderValue) {

    ResponseEntity<TestResource> actual =
        client.getForEntity(getServerBaseUrl() + "/api/resource", TestResource.class);
    assertThat(actual)
        .isNotNull()
        .extracting(ResponseEntity::getStatusCode)
        .isEqualTo(HttpStatus.OK);

    assertThat(actual)
        .extracting(HttpEntity::getHeaders)
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsEntry(predefinedHeaderName, List.of(expectedPredefinedHeaderValue));
  }

  @ParameterizedTest
  @MethodSource("predefinedRestfulApiSecurityHeaders")
  void shouldAllowOverwritingHeaders(String predefinedHeaderName) {
    // for unknown reason, X-Frame-Options can't be modified
    assumeThat(predefinedHeaderName).isNotEqualTo("X-Frame-Options");

    ResponseEntity<TestResource> actual =
        client.getForEntity(
            getServerBaseUrl() + "/api/header?headerName={headerName}&headerValue={headerValue}",
            TestResource.class,
            Map.of("headerName", predefinedHeaderName, "headerValue", "CUSTOM_VALUE"));
    assertThat(actual)
        .isNotNull()
        .extracting(ResponseEntity::getStatusCode)
        .isEqualTo(HttpStatus.OK);

    assertThat(actual)
        .extracting(HttpEntity::getHeaders)
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsEntry(predefinedHeaderName, List.of("CUSTOM_VALUE"));
  }

  /**
   * This test verifies that no headers are exposed, that identify Spring Boot or other components
   * of the service.
   */
  @Test
  void verifyExistingDefaultHeaders() {
    ResponseEntity<TestResource> actual =
        client.getForEntity(getServerBaseUrl() + "/api/resource", TestResource.class);
    assertThat(actual)
        .isNotNull()
        .extracting(ResponseEntity::getStatusCode)
        .isEqualTo(HttpStatus.OK);
    HttpHeaders actualHeaders = actual.getHeaders();
    assertThat(actualHeaders)
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsOnlyKeys(
            Stream.concat(
                    Stream.of(
                        "Content-Type",
                        "Transfer-Encoding",
                        "Date",
                        "Keep-Alive",
                        "Connection",
                        "Vary",
                        "Trace-Token"),
                    predefinedSecurityHeaders().map(Arguments::get).map(it -> it[0]))
                .collect(Collectors.toList()));
  }

  /**
   * This test verifies that no headers are exposed, that identify Spring Boot or other components
   * of the service.
   */
  @Test
  void verifyExistingDefaultHeadersInCaseOfError() {
    ResponseEntity<TestResource> actual =
        client.getForEntity(getServerBaseUrl() + "/api/does/not/exist", TestResource.class);
    assertThat(actual)
        .isNotNull()
        .extracting(ResponseEntity::getStatusCode)
        .isEqualTo(HttpStatus.NOT_FOUND);
    HttpHeaders actualHeaders = actual.getHeaders();
    assertThat(actualHeaders)
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsOnlyKeys(
            Stream.concat(
                    Stream.of(
                        "Content-Type",
                        "Transfer-Encoding",
                        "Date",
                        "Keep-Alive",
                        "Connection",
                        "Vary",
                        "Trace-Token"),
                    predefinedSecurityHeaders().map(Arguments::get).map(it -> it[0]))
                .collect(Collectors.toList()));
  }

  @Test
  void verifyAllowedMethods() {
    List<HttpMethod> httpMethods =
        client.optionsForAllow(getServerBaseUrl() + "/api/resource").stream().toList();
    assertThat(httpMethods)
        .asList()
        .containsExactlyInAnyOrder(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS);
  }

  String getServerBaseUrl() {
    return String.format("http://localhost:%s", port);
  }
}
