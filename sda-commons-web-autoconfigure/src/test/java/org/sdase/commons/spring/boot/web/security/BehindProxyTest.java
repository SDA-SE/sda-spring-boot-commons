/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.security.test.SecurityTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = SecurityTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
class BehindProxyTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @Autowired private AuthMock authMock;

  @BeforeEach
  void allow() {
    authMock.authorizeAnyRequest().allow();
  }

  @Test
  void useRegularIpWithoutForwardedByHeader() {
    HttpHeaders headers = acceptTextPlainHeaders();
    String caller = getCaller(headers);
    assertThat(caller).contains("127.0.0.1");
  }

  @Test
  void useForwardedForHeader() {
    HttpHeaders headers = acceptTextPlainHeaders();
    headers.set("X-Forwarded-For", "192.168.123.123");
    String caller = getCaller(headers);
    assertThat(caller).contains("192.168.123.123");
  }

  @Test
  void createLinkWithoutForwardedProtoAndHostHeader() {
    HttpHeaders headers = acceptTextPlainHeaders();
    String caller = getLink(headers);
    assertThat(caller).contains("http://localhost:" + port);
  }

  @Test
  void useForwardedProtoAndHostHeaderToCreateLink() {
    HttpHeaders headers = acceptTextPlainHeaders();
    headers.set("X-Forwarded-Proto", "https");
    headers.set("X-Forwarded-Host", "from.external.example.com");
    String caller = getLink(headers);
    assertThat(caller).contains("https://from.external.example.com");
  }

  private static HttpHeaders acceptTextPlainHeaders() {
    var headers = new HttpHeaders();
    headers.setAccept(List.of(org.springframework.http.MediaType.TEXT_PLAIN));
    return headers;
  }

  private String getCaller(HttpHeaders headers) {
    return client
        .exchange(
            getServerBaseUrl() + "/api/caller",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class)
        .getBody();
  }

  private String getLink(HttpHeaders headers) {
    return client
        .exchange(
            getServerBaseUrl() + "/api/link",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            String.class)
        .getBody();
  }

  String getServerBaseUrl() {
    return String.format("http://localhost:%s", port);
  }
}
