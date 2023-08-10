/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.platform;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;
import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.sdase.commons.spring.boot.web.platform.test.PlatformClientService;
import org.sdase.commons.spring.boot.web.platform.test.PlatformClientTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.client.EnableSdaOidcClientMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@SetSystemProperty(key = "enable.json.logging", value = "true")
@SpringBootTest(
    classes = PlatformClientTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "platformClient.baseUrl=http://localhost:${wiremock.server.port}/api",
      "oidc.client.enabled=true"
    })
@AutoConfigureWireMock(port = 0)
@ContextConfiguration(initializers = {EnableSdaOidcClientMockInitializer.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class PlatformClientTest {
  @LocalServerPort private int port;
  @Autowired ObjectMapper objectMapper;
  @Autowired private AuthMock authMock;
  @Autowired private PlatformClientService platformClientService;

  private static final String DISCOVERY_PATH = "/issuer/.well-known/openid-configuration";
  private static final String JWKS_PATH = "/issuer/keys";
  private static final String TOKEN_PATH = "/issuer/token";

  @BeforeEach
  void setUp() {
    WireMock.reset();
    authMock.reset();
    setupAuthMock();
  }

  // SdaTraceTokenClientConfiguration
  @Test
  void traceTokenEnabled() {
    // given
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/hello"))
            .withHeader("Trace-Token", equalTo("pre-defined-trace-token"))
            .willReturn(ResponseDefinitionBuilder.okForJson(Map.of("hello", "world"))));

    var headers = new HttpHeaders();
    headers.set("Trace-Token", "pre-defined-trace-token");

    var authentication = authMock.authentication();

    // when
    ResponseEntity<String> responseEntity =
        executeRequestWithHeader(authentication.authenticatedClient(), "/api/proxy", port, headers);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  // AuthenticationPassThroughClientConfiguration
  @Test
  void authenticationPassThroughEnabled() {
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/hello"))
            .willReturn(ResponseDefinitionBuilder.okForJson(Map.of("hello", "world"))));

    var authentication = authMock.authentication();

    // when
    executeRequestWithHeader(
        authentication.authenticatedClient(), "/api/proxy", port, new HttpHeaders());

    authMock
        .wireMockServer()
        .verify(0, WireMock.postRequestedFor(WireMock.urlPathEqualTo(TOKEN_PATH)));
  }

  // OidcClientRequestConfiguration
  @Test
  void oidcClientEnabled() {
    // given
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/hello"))
            .willReturn(ResponseDefinitionBuilder.okForJson(Map.of("hello", "world"))));

    // when
    var response = platformClientService.getSomething();

    // then
    assertThat(response).isNotNull();

    WireMock.verify(
        WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/hello"))
            .withHeader("Authorization", matching(".*")));

    authMock
        .wireMockServer()
        .verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(TOKEN_PATH)));
  }

  private ResponseEntity<String> executeRequestWithHeader(
      TestRestTemplate client, String path, int port, HttpHeaders headers) {
    return client.exchange(
        String.format("http://localhost:%d%s", port, path),
        HttpMethod.GET,
        new HttpEntity<>(headers),
        String.class);
  }

  private void setupAuthMock() {
    authMock.authorizeAnyRequest().allow();

    authMock
        .wireMockServer()
        .addStubMapping(
            WireMock.request("GET", WireMock.urlPathEqualTo(DISCOVERY_PATH))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(STARTED)
                .willSetStateTo("Cause Success")
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(
                            Json.write(
                                Map.of(
                                    "issuer",
                                    String.format("%s/issuer", authMock.wireMockServer().baseUrl()),
                                    "jwks_uri",
                                    String.format(
                                        "%s%s", authMock.wireMockServer().baseUrl(), JWKS_PATH),
                                    "token_endpoint",
                                    String.format(
                                        "%s%s", authMock.wireMockServer().baseUrl(), TOKEN_PATH),
                                    "subject_types_supported",
                                    List.of("public", "pairwise"))))
                        .withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType()))
                .build());
  }
}
