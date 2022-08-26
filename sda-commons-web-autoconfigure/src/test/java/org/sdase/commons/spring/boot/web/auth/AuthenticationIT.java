/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.nimbusds.jose.PlainHeader;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.PlainJWT;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = AuthTestApp.class,
    webEnvironment = RANDOM_PORT,
    properties = {"management.server.port=0"})
@ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
class AuthenticationIT {

  @Value("${auth.issuers}")
  private String trustedIssuers;

  @Autowired private AuthMock authMock;

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @BeforeEach
  void reset() {
    authMock.reset();
  }

  @Test
  void shouldAcceptAuthenticatedRequest() {
    authMock.authorizeAnyRequest().allow();
    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).extracting("authenticated").isEqualTo(true);
  }

  @Test
  void shouldProvideAdminConstraints() {
    authMock.authorizeAnyRequest().allowWithConstraint(Map.of("admin", true));
    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).extracting("admin").isEqualTo(true);
  }

  @Test
  void shouldResetAdminConstraintsForSecondRequest() {
    authMock.authorizeAnyRequest().allowWithConstraint(Map.of("admin", true));
    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).extracting("admin").isEqualTo(true);
    authMock.authorizeAnyRequest().allow();
    var nextResponse =
        authMock
            .authentication()
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    assertThat(nextResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(nextResponse.getBody()).extracting("admin").isEqualTo(false);
  }

  @Test
  void shouldRejectAuthenticatedRequestWithOpaDeny() {
    authMock.authorizeAnyRequest().deny();
    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void shouldSendDataForAuthorizationToOpa() {
    var authentication = authMock.authentication();
    authMock
        .authorizeRequest()
        .withHttpMethod("GET")
        .withPath("/ping/real")
        .withJwtFromHeaderValue(authentication.headerValue())
        .allow();
    var response =
        authentication
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/ping/real", Object.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).extracting("authenticated").isEqualTo(true);
    authMock
        .wireMockServer()
        .verify(
            postRequestedFor(urlMatching("/v1/data/org/sdase/commons/spring/boot/web/auth"))
                .withRequestBody(matchingJsonPath("$.input.httpMethod", equalTo("GET")))
                .withRequestBody(matchingJsonPath("$.input.path[0]", equalTo("ping")))
                .withRequestBody(matchingJsonPath("$.input.path[1]", equalTo("real")))
                .withRequestBody(matchingJsonPath("$.input.jwt", equalTo(authentication.token())))
                .withRequestBody(
                    matchingJsonPath(
                        "$.input.headers.authorization[0]",
                        equalTo("Bearer " + authentication.token())))
                .withRequestBody(
                    matchingJsonPath("$.input.headers.connection[0]", equalTo("Keep-Alive"))));
  }

  @Test
  void shouldAcceptAnonymousRequest() {
    authMock.authorizeRequest().withHttpMethod("GET").withPath("/ping").allow();
    var response = client.getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).extracting("authenticated").isEqualTo(false);
  }

  @Test
  void shouldRejectAnonymousRequest() {
    authMock.authorizeRequest().withHttpMethod("GET").withPath("/ping").deny();
    var response = client.getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    // Spring converts authorization errors of unauthenticated users to 401
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldRejectAlgNone() {
    var payloadBuilder =
        new JWTClaimsSet.Builder()
            .issuer(this.trustedIssuers.trim())
            .jwtID(UUID.randomUUID().toString())
            .subject("evil");
    var plainJwt = new PlainJWT(new PlainHeader(), payloadBuilder.build());
    var jwt = plainJwt.serialize();
    var headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", jwt));
    authMock.authorizeRequest().withHttpMethod("GET").withPath("/ping").allow();

    var response =
        client.exchange(
            "http://localhost:" + port + "/api/ping",
            HttpMethod.GET,
            new HttpEntity<>(headers),
            Object.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldNotCreateSession() {
    var response =
        authMock
            .authentication()
            .authenticatedClient()
            .getForEntity("http://localhost:" + port + "/api/ping", Object.class);
    assertThat(response.getHeaders())
        .doesNotContainKeys(HttpHeaders.COOKIE, HttpHeaders.SET_COOKIE, HttpHeaders.SET_COOKIE2);
  }
}
