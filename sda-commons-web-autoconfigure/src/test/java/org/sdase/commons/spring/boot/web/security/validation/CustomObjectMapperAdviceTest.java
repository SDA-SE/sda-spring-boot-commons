/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.validation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.jackson.SdaObjectMapperConfiguration;
import org.sdase.commons.spring.boot.web.security.exception.InsecureConfigurationException;
import org.sdase.commons.spring.boot.web.security.test.ContextUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

class CustomObjectMapperAdviceTest {
  @Test
  void shouldPreventStartupIfTracingIsEnabled() {
    assertThat(ContextUtils.createTestContext(NoSdaObjectMapperApp.class))
        .hasFailed()
        .getFailure()
        .getRootCause()
        .isInstanceOf(InsecureConfigurationException.class)
        .hasMessage(
            "Missing sdaObjectMapperBuilder bean from org.sdase.commons.spring.boot.web.jackson. "
                + "The Jackson2ObjectMapperBuilder component registers custom mappers.");
  }

  @SpringBootApplication(exclude = SdaObjectMapperConfiguration.class)
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  public static class NoSdaObjectMapperApp {}
}
