/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.auth.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.auth.test.AuthTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = AuthTestApp.class,
    webEnvironment = RANDOM_PORT,
    properties = {"management.server.port=0"})
@ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
class AuthMockTest {

  @Autowired AuthMock authMock;

  @Autowired private TestRestTemplate client;

  @Test
  void shouldProvideDiscovery() {
    var baseUrl = authMock.wireMockServer().baseUrl();
    var openIdConfiguration =
        client.getForObject(baseUrl + "/issuer/.well-known/openid-configuration", Object.class);
    assertThat(openIdConfiguration)
        .extracting("jwks_uri", "issuer")
        .containsExactly(baseUrl + "/issuer/keys", baseUrl + "/issuer");
  }

  @Test
  void shouldProvideKeys() throws ParseException {
    final String exampleToken = authMock.authentication().token();
    var kid = JWTParser.parse(exampleToken).getHeader().toJSONObject().get("kid");
    var baseUrl = authMock.wireMockServer().baseUrl();
    var keys = client.getForObject(baseUrl + "/issuer/keys", Object.class);
    assertThat(keys)
        .extracting("keys")
        .asList()
        .extracting("kty", "use", "kid")
        .containsExactly(tuple("RSA", "sig", kid));
  }
}
