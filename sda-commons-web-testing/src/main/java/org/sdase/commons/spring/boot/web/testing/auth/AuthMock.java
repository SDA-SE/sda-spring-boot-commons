/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing.auth;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.CONTENT_TYPE;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.BasicCredentials;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import java.util.List;
import java.util.Map;
import org.sdase.commons.spring.boot.web.testing.auth.opa.AllowBuilder;
import org.sdase.commons.spring.boot.web.testing.auth.opa.RequestMethodBuilder;
import org.sdase.commons.spring.boot.web.testing.auth.opa.StubBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthMock {

  private static final Logger LOG = LoggerFactory.getLogger(AuthMock.class);

  private static final String DISCOVERY_PATH = "/issuer/.well-known/openid-configuration";
  private static final String JWKS_PATH = "/issuer/keys";
  private static final String TOKEN_PATH = "/issuer/token";
  private static final String DEFAULT_SUBJECT = "john_doe";
  private static final String CLIENT_ID = "oidcClient";
  private static final String CLIENT_SECRET = "s3cret";

  private final String issuer;
  private final WireMockServer server;
  private final ObjectMapper om;

  private RsaKeyPair keyPair;
  private String accessToken;

  public AuthMock(WireMockServer server, ObjectMapper om) {
    this.issuer = String.format("%s/issuer", server.baseUrl());
    this.server = server;
    this.om = om;
    LOG.info("Providing JWT as issuer {}", issuer);
    reset();
  }

  /** Creates a new keypair for signing and removes the existing. */
  public void rotateKey() {
    this.keyPair = new RsaKeyPair();
    this.accessToken = authentication().token();
    initJwks();
    initToken();
  }

  /** {@linkplain #rotateKey() Rotates the key for signing} and initializes the server again. */
  public void reset() {
    rotateKey();
    server.resetAll();
    initDiscovery();
    initJwks();
    initToken();
  }

  /**
   * @return a builder to configure JWT content and create a signed token that will be accepted by
   *     the application
   */
  public AuthBuilder authentication() {
    return new AuthBuilder(this.keyPair, this.issuer).withSubject(DEFAULT_SUBJECT);
  }

  public RequestMethodBuilder authorizeRequest() {
    return new StubBuilder(wireMockServer(), om, false);
  }

  public AllowBuilder authorizeAnyRequest() {
    return new StubBuilder(wireMockServer(), om, true);
  }

  // only for testing this class
  public WireMockServer wireMockServer() {
    return this.server;
  }

  private void initDiscovery() {
    server.addStubMapping(
        WireMock.request("GET", WireMock.urlPathEqualTo(DISCOVERY_PATH))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(
                        Json.write(
                            Map.of(
                                "issuer",
                                this.issuer,
                                "jwks_uri",
                                String.format("%s%s", server.baseUrl(), JWKS_PATH),
                                "token_endpoint",
                                String.format("%s%s", server.baseUrl(), TOKEN_PATH),
                                "subject_types_supported",
                                List.of("public", "pairwise"))))
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType()))
            .build());
  }

  private void initJwks() {
    server.addStubMapping(
        WireMock.request("GET", WireMock.urlMatching(JWKS_PATH))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(Json.write(Map.of("keys", List.of(keyPair.getPublicKeyForJwks()))))
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType()))
            .build());
  }

  private void initToken() {
    server.addStubMapping(
        WireMock.request("POST", WireMock.urlMatching(TOKEN_PATH))
            .withHeader(
                AUTHORIZATION,
                equalTo(
                    new BasicCredentials(CLIENT_ID, CLIENT_SECRET).asAuthorizationHeaderValue()))
            .withRequestBody(equalTo("grant_type=client_credentials"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType())
                    .withBody(
                        Json.write(
                            Map.of(
                                "access_token",
                                providedAccessToken(),
                                "token_type",
                                "Bearer",
                                "expires_in",
                                3600)))
                    .withHeader(CONTENT_TYPE, APPLICATION_JSON.getMimeType()))
            .build());
  }

  /**
   * @return the provided access token when requesting the token endpoint.
   */
  public String providedAccessToken() {
    return this.accessToken;
  }
}
