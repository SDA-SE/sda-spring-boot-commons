/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.validation.trace;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.security.exception.InsecureConfigurationException;
import org.sdase.commons.spring.boot.web.security.test.ContextUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

class SecureMethodsAdviceTest {

  @Test
  void shouldPreventStartupIfTracingIsEnabled() {
    Assertions.assertThat(ContextUtils.createTestContext(TraceAllowedApp.class))
        .hasFailed()
        .getFailure()
        .getRootCause()
        .isInstanceOfSatisfying(
            InsecureConfigurationException.class,
            e -> assertThat(e.getMessage()).isEqualTo("The server accepts insecure methods."));
  }

  @SpringBootApplication
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  @ContextConfiguration(classes = {AllowTraceMethodConfig.class})
  public static class TraceAllowedApp {}
}
