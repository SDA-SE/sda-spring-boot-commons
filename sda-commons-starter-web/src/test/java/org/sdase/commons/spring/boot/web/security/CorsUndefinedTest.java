/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.security.test.SecurityTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

@SpringBootTest(
    classes = SecurityTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
class CorsUndefinedTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @Test
  void verifyCorsNotMatching() {
    var headers = new HttpHeaders();
    headers.set("Access-Control-Request-Method", "GET");
    headers.set("Access-Control-Request-Headers", "origin, authorization");
    headers.set("Origin", "https://external.example.com");

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
