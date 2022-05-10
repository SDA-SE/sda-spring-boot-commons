/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.auth.opa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.auth.AuthTestApp;
import org.sdase.commons.spring.boot.web.auth.MyConstraints;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.context.request.RequestContextHolder;

@SpringBootTest(classes = AuthTestApp.class)
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
class AbstractConstraintsTest {

  @Autowired private MyConstraints myConstraints;

  @Test
  void shouldFailSilentIfNotInARequestContext() {
    //noinspection ResultOfMethodCallIgnored
    assertThatCode(() -> myConstraints.isAdmin()).doesNotThrowAnyException();
  }

  @Test
  void shouldHaveDefaultValuesIfNotInRequestContext() {
    assertThat(myConstraints.isAdmin()).isFalse();
  }

  @Test
  void shouldNotFailForWrongType() {
    var abstractConstraints = new AbstractConstraints() {};

    RequestContextHolder.currentRequestAttributes()
        .setAttribute(OpaAccessDecisionVoter.CONSTRAINTS_ATTRIBUTE, new Object(), 0);
    try {
      assertThatCode(abstractConstraints::process).doesNotThrowAnyException();
    } finally {
      RequestContextHolder.currentRequestAttributes()
          .removeAttribute(OpaAccessDecisionVoter.CONSTRAINTS_ATTRIBUTE, 0);
    }
  }

  @Test
  void shouldNotFailForNull() {
    var abstractConstraints = new AbstractConstraints() {};
    assertThatCode(abstractConstraints::process).doesNotThrowAnyException();
  }
}
