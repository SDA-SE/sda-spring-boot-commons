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
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = ClientCredentialsTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "feign.test.api.base.url=http://localhost:${wiremock.server.port}",
      "oidc.client.enabled=true"
    })
@ContextConfiguration(initializers = EnableSdaOidcClientMockInitializer.class)
@AutoConfigureWireMock
class ClientCredentialsFlowIT {

  @Autowired ClientCredentialsFlowFeignClient testClient;

  @Autowired AuthMock authMock;

  @Autowired ObjectMapper objectMapper;

  @LocalServerPort int port;

  @Test
  void shouldUseAccessTokenFromOidcProvider() {
    stubFor(
        get("/pong")
            .withHeader(AUTHORIZATION, equalTo("Bearer " + authMock.providedAccessToken()))
            .willReturn(ok()));

    authMock.authorizeAnyRequest().allow();

    var request = new TestRestTemplate(new RestTemplateBuilder());
    request.getForObject(
        String.format("http://localhost:%d/api/ping/external", port), Object.class);

    verify(
        getRequestedFor(urlMatching("/pong"))
            .withHeader(AUTHORIZATION, equalTo("Bearer " + authMock.providedAccessToken())));
  }

  @Test
  void shouldUseAccessTokenFromRequestContext() {
    var authentication = authMock.authentication();
    stubFor(
        get("/pong")
            .withHeader(AUTHORIZATION, equalTo(authentication.headerValue()))
            .willReturn(ok()));

    authMock.authorizeAnyRequest().allow();

    authentication
        .authenticatedClient()
        .getForObject(String.format("http://localhost:%d/api/ping/external", port), Object.class);

    verify(
        getRequestedFor(urlMatching("/pong"))
            .withHeader(AUTHORIZATION, equalTo(authentication.headerValue())));
  }
}
