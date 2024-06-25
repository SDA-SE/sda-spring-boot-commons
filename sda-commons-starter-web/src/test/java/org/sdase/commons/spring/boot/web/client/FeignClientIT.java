/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.*;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.client.test.ClientTestApp;
import org.sdase.commons.spring.boot.web.client.test.ClientTestConstraints;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = ClientTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "other.baseUrl=http://localhost:${wiremock.server.port}/api",
      "otherAuthenticated.baseUrl=http://localhost:${wiremock.server.port}/api",
    })
@AutoConfigureWireMock(port = 0)
@ContextConfiguration(initializers = {EnableSdaAuthMockInitializer.class})
class FeignClientIT {

  @Autowired private AuthMock authMock;

  @LocalServerPort private int port;
  @Autowired private TestRestTemplate client;

  @BeforeEach
  void setUp() {
    WireMock.reset();
  }

  @Test
  void shouldCallOtherServiceWithoutAuthentication() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/hello"))
            .willReturn(ResponseDefinitionBuilder.okForJson(Map.of("hello", "world"))));

    authMock.authorizeAnyRequest().allow();
    var actual =
        authMock
            .authentication()
            .authenticatedClient()
            .getForObject(String.format("http://localhost:%d/api/proxy", port), Object.class);

    assertThat(actual).extracting("hello").isEqualTo("world");

    WireMock.verify(
        WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/hello"))
            .withoutHeader("Authorization"));
  }

  @Test
  void shouldCallOtherServiceWithAuthentication() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/hello"))
            .willReturn(ResponseDefinitionBuilder.okForJson(Map.of("hello", "world"))));

    authMock.authorizeAnyRequest().allow();
    var authentication = authMock.authentication();
    var actual =
        authentication
            .authenticatedClient()
            .getForObject(String.format("http://localhost:%d/api/authProxy", port), Object.class);

    assertThat(actual).extracting("hello").isEqualTo("world");

    WireMock.verify(
        WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/hello"))
            .withHeader("Authorization", WireMock.equalTo(authentication.headerValue())));
  }

  @Test
  void shouldIgnoreMissingAuthentication() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/hello"))
            .willReturn(ResponseDefinitionBuilder.okForJson(Map.of("hello", "world"))));

    authMock.authorizeAnyRequest().allow();
    var actual =
        client.getForObject(String.format("http://localhost:%d/api/authProxy", port), Object.class);

    assertThat(actual).extracting("hello").isEqualTo("world");

    WireMock.verify(
        WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/hello"))
            .withoutHeader("Authorization"));
  }

  @Test
  void shouldCallAsyncWithAuthentication() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/hello"))
            .willReturn(
                ResponseDefinitionBuilder.responseDefinition()
                    .withUniformRandomDelay(1, 20)
                    .withHeader("Content-type", "application/json")
                    .withBody(Json.write(Map.of("hello", "world")))));

    var authentication = authMock.authentication();
    authMock
        .authorizeAnyRequest()
        .allowWithConstraint(new ClientTestConstraints().setCallAsyncAllowed(true));
    var actual =
        authentication
            .authenticatedClient()
            .getForObject(
                String.format("http://localhost:%d/api/authProxyAsync", port), Object.class);

    WireMock.verify(
        100,
        WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/hello"))
            .withHeader("Authorization", WireMock.equalTo(authentication.headerValue())));

    boolean sorted = false;
    try {
      assertThat(actual)
          .asInstanceOf(InstanceOfAssertFactories.LIST)
          .hasSize(100)
          .extracting("time")
          .isSorted();
      sorted = true;
    } catch (AssertionError ignored) {
      // ignore
    }
    assertThat(sorted)
        .describedAs("Expecting asynchronously loaded data is not sorted: %s", actual)
        .isFalse();
  }
}
