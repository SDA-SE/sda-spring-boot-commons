/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sdase.commons.spring.boot.web.security.test.SecurityTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = SecurityTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties =
        "cors.allowed-origin-patterns=https://allowed.com, https://*.foo.com, https://foo-*.bar.com")
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
@AutoConfigureTestRestTemplate
class CorsOriginsTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @ParameterizedTest
  @ValueSource(
      strings = {
        "https://allowed.com",
        "https://bar.foo.com",
        "https://deep.matching.foo.com",
        "https://foo-pr-1.bar.com"
      })
  void verifyCorsAllowed(String givenOrigin) {
    var headers = new HttpHeaders();
    headers.set("Access-Control-Request-Method", "GET");
    headers.set("Access-Control-Request-Headers", "origin, authorization");
    headers.set("Origin", givenOrigin);

    var actual =
        client.exchange(
            getServerBaseUrl() + "/api/resource",
            HttpMethod.OPTIONS,
            new HttpEntity<>(headers),
            String.class);
    assertThat(actual.getHeaders().containsHeaderValue("Access-Control-Allow-Origin", givenOrigin));
  }

  @ParameterizedTest
  @ValueSource(strings = {"https://unknown.com", "https://not-matching.allowed.com"})
  void verifyCorsNotMatching(String givenOrigin) {
    var headers = new HttpHeaders();
    headers.set("Access-Control-Request-Method", "GET");
    headers.set("Access-Control-Request-Headers", "origin, authorization");
    headers.set("Origin", givenOrigin);

    var actual =
        client.exchange(
            getServerBaseUrl() + "/api/resource",
            HttpMethod.OPTIONS,
            new HttpEntity<>(headers),
            String.class);
    assertThat(actual.getHeaders().containsHeader("Access-Control-Allow-Origin")).isFalse();
  }

  String getServerBaseUrl() {
    return String.format("http://localhost:%s", port);
  }
}
