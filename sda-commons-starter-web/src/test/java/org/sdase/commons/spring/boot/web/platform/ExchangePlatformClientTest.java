/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.platform;

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
import org.sdase.commons.spring.boot.web.client.Employee;
import org.sdase.commons.spring.boot.web.client.MyService;
import org.sdase.commons.spring.boot.web.platform.test.PlatformClientTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.client.EnableSdaOidcClientMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.reactive.function.client.WebClient;

@SetSystemProperty(key = "enable.json.logging", value = "true")
@SpringBootTest(
    classes = PlatformClientTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "platformClient.baseUrl=http://localhost:${wiremock.server.port}/api",
      "platform.client.baseUrl=http://localhost:${wiremock.server.port}/api",
      "oidc.client.enabled=true"
    })
@AutoConfigureWireMock(port = 0)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
@ContextConfiguration(initializers = {EnableSdaOidcClientMockInitializer.class})
class ExchangePlatformClientTest {
  @LocalServerPort private int port;
  @Autowired ObjectMapper objectMapper;
  @Autowired private AuthMock authMock;
  @Autowired private WebClient platformClient;
  @Autowired private MyService myService;

  private static final String DISCOVERY_PATH = "/issuer/.well-known/openid-configuration";
  private static final String JWKS_PATH = "/issuer/keys";
  private static final String TOKEN_PATH = "/issuer/token";

  @BeforeEach
  void setUp() {
    WireMock.reset();
    authMock.reset();
    setupAuthMock();
  }

  // OidcClientHttpExchangeFilter
  @Test
  void oidcClientEnabled() {
    // given
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/hello"))
            .willReturn(ResponseDefinitionBuilder.okForJson(Map.of("hello", "world"))));

    // when
    ResponseEntity<String> response =
        platformClient
            .get()
            .uri("/hello")
            .header("Authorization", "test123")
            .retrieve()
            .toEntity(
                String.class) // you can handle the response in the client or service, but also like
            // in this example, straight from the webClient call
            .block(); // handling the response synchronously

    // then
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

    WireMock.verify(
        WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/hello"))
            .withHeader("Authorization", matching(".*")));

    authMock
        .wireMockServer()
        .verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(TOKEN_PATH)));
  }

  @Test
  void postEmployee() {
    // given
    WireMock.stubFor(
        WireMock.post(WireMock.urlPathEqualTo("/api/employees"))
            .willReturn(ResponseDefinitionBuilder.okForJson(new Employee("Joseph", "Developer"))));

    // when
    // an example using a Service containing the webClient and handling the response internally
    var response =
        myService
            .createEmployee(new Employee("Joseph", "Developer"))
            .block(); // handling the response synchronously

    // then
    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo("Joseph");
    assertThat(response.position()).isEqualTo("Developer");

    WireMock.verify(
        WireMock.postRequestedFor(WireMock.urlPathEqualTo("/api/employees"))
            .withHeader("Authorization", matching(".*")));

    authMock
        .wireMockServer()
        .verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(TOKEN_PATH)));
  }

  @Test
  void getEmployeeById() {
    // given
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/employees/1"))
            .willReturn(ResponseDefinitionBuilder.okForJson(new Employee("Joseph", "Developer"))));

    // when
    // an example using a Service containing the webClient and handling the response internally with
    // a custom method
    var response = myService.fetchEmployeeById(1).block(); // handling the response synchronously

    // then
    assertThat(response).isNotNull();
    assertThat(response.name()).isEqualTo("Joseph");
    assertThat(response.position()).isEqualTo("Developer");

    WireMock.verify(
        WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/employees/1"))
            .withHeader("Authorization", matching(".*")));

    authMock
        .wireMockServer()
        .verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(TOKEN_PATH)));
  }

  @Test
  void getAllEmployees() {
    // given
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/employees"))
            .willReturn(
                ResponseDefinitionBuilder.okForJson(List.of(new Employee("Joseph", "Developer")))));

    // when
    // an example using a Service containing the annotated interface webClient
    var response = myService.fetchAllEmployees();

    // then
    assertThat(response).isNotNull().isNotEmpty();
    assertThat(response.get(0).name()).isEqualTo("Joseph");
    assertThat(response.get(0).position()).isEqualTo("Developer");

    WireMock.verify(
        WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/employees"))
            .withHeader("Authorization", matching(".*")));

    authMock
        .wireMockServer()
        .verify(1, WireMock.postRequestedFor(WireMock.urlPathEqualTo(TOKEN_PATH)));
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
