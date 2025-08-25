/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@EnableConfigurationProperties(value = AuthProperties.class)
class AuthPropertiesTest {

  @Nested
  class DefaultValues {

    @Autowired private AuthProperties authProperties;

    @Test
    void shouldHaveCorrectDefaults() {
      assertThat(authProperties.getDisable()).isFalse();
      assertThat(authProperties.getIssuers()).isNotNull().isEmpty();
    }
  }

  @Nested
  @TestPropertySource(properties = {"auth.disable=true", "auth.issuers=issuer1,issuer2"})
  class CustomValues {
    @Autowired private AuthProperties authProperties;

    @Test
    void shouldLoadProperties() {
      assertThat(authProperties.getDisable()).isTrue();
      assertThat(authProperties.getIssuers()).isNotNull();
      assertThat(authProperties.getIssuers()).hasSize(2);
      assertThat(authProperties.getIssuers()).contains("issuer1", "issuer2");
    }
  }
}
