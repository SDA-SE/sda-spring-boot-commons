/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED;
import static jakarta.ws.rs.core.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.client.oidc.ClientCredentialsFlowFeignClient;
import org.sdase.commons.spring.boot.web.client.oidc.ClientCredentialsTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.client.EnableSdaOidcClientMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = ClientCredentialsTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "feign.test.api.base.url=http://localhost:${wiremock.server.port}",
      "oidc.client.enabled=true"
    })
@ContextConfiguration(initializers = EnableSdaOidcClientMockInitializer.class)
@AutoConfigureWireMock(port = 0)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class ClientCredentialsFlowIT {

  private static final String DISCOVERY_PATH = "/issuer/.well-known/openid-configuration";
  private static final String JWKS_PATH = "/issuer/keys";
  private static final String TOKEN_PATH = "/issuer/token";

  @Autowired ClientCredentialsFlowFeignClient testClient;

  @Autowired AuthMock authMock;

  @Autowired ObjectMapper objectMapper;

  @LocalServerPort int port;

  @BeforeEach
  void beforeEach() {
    WireMock.reset();
    authMock.reset();
  }

  @Test
  void shouldUseAccessTokenFromOidcProvider() {
    // given
    stubFor(
        get("/pong")
            .withHeader(AUTHORIZATION, equalTo("Bearer " + authMock.providedAccessToken()))
            .willReturn(ok()));

    authMock.authorizeAnyRequest().allow();

    // when
    var request = new TestRestTemplate(new RestTemplateBuilder());
    request.getForObject(
        String.format("http://localhost:%d/api/ping/external", port), Object.class);

    // then
    verify(
        getRequestedFor(urlMatching("/pong"))
            .withHeader(AUTHORIZATION, equalTo("Bearer " + authMock.providedAccessToken())));
  }

  @Test
  void shouldUseAccessTokenFromRequestContext() {
    // given
    var authentication = authMock.authentication();
    stubFor(
        get("/pong")
            .withHeader(AUTHORIZATION, equalTo(authentication.headerValue()))
            .willReturn(ok()));

    authMock.authorizeAnyRequest().allow();

    // when
    authentication
        .authenticatedClient()
        .getForObject(String.format("http://localhost:%d/api/ping/external", port), Object.class);

    // then
    verify(
        getRequestedFor(urlMatching("/pong"))
            .withHeader(AUTHORIZATION, equalTo(authentication.headerValue())));
  }

  @Test
  void shouldUseAccessTokenFromRequestContextWithRetry() {
    // given
    stubFor(get("/pong").willReturn(ok()));

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

    authMock
        .wireMockServer()
        .addStubMapping(
            WireMock.request("GET", WireMock.urlPathEqualTo(DISCOVERY_PATH))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Cause Success")
                .willReturn(WireMock.aResponse().withStatus(503))
                .build());

    // when
    var request = new TestRestTemplate(new RestTemplateBuilder());
    request.getForObject(
        String.format("http://localhost:%d/api/ping/external", port), Object.class);

    // then
    verify(1, getRequestedFor(urlMatching("/pong")));

    verify(
        0,
        getRequestedFor(urlMatching("/pong"))
            .withHeader(
                AUTHORIZATION,
                equalTo(String.format("Bearer %s", authMock.providedAccessToken()))));

    authMock.wireMockServer().verify(2, getRequestedFor(urlPathEqualTo(DISCOVERY_PATH)));

    // given
    authMock.reset();
    authMock.authorizeAnyRequest().allow();

    // when
    request.getForObject(
        String.format("http://localhost:%d/api/ping/external", port), Object.class);

    // then
    authMock.wireMockServer().verify(2, getRequestedFor(urlPathEqualTo(DISCOVERY_PATH)));

    verify(
        getRequestedFor(urlMatching("/pong"))
            .withHeader(
                AUTHORIZATION,
                equalTo(String.format("Bearer %s", authMock.providedAccessToken()))));
  }

  @Test
  void shouldUseAccessTokenFromRequestContextWithCache() {
    // given
    var token = String.format("Bearer %s", authMock.providedAccessToken());

    stubFor(get("/pong").withHeader(AUTHORIZATION, equalTo(token)).willReturn(ok()));

    authMock.authorizeAnyRequest().allow();

    // when
    var request = new TestRestTemplate(new RestTemplateBuilder());
    request.getForObject(
        String.format("http://localhost:%d/api/ping/external", port), Object.class);

    // then
    verify(getRequestedFor(urlMatching("/pong")).withHeader(AUTHORIZATION, equalTo(token)));

    authMock.wireMockServer().verify(2, getRequestedFor(urlPathEqualTo(DISCOVERY_PATH)));

    // given
    authMock.reset();

    stubFor(get("/pong").withHeader(AUTHORIZATION, equalTo(token)).willReturn(ok()));

    authMock.authorizeAnyRequest().allow();

    // when
    request.getForObject(
        String.format("http://localhost:%d/api/ping/external", port), Object.class);

    // then
    verify(getRequestedFor(urlMatching("/pong")).withHeader(AUTHORIZATION, equalTo(token)));

    authMock.wireMockServer().verify(0, getRequestedFor(urlPathEqualTo(DISCOVERY_PATH)));
  }
}
