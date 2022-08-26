/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
class AuthMockTest {

  @Autowired AuthMock authMock;

  private final TestRestTemplate client = new TestRestTemplate();

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
