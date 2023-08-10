/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = AuthTestApp.class, webEnvironment = RANDOM_PORT)
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
class NoDevelopUserCreatedTest {

  @Autowired ApplicationContext applicationContext;

  @Test
  void shouldNotHaveBeanWithGeneratedUser() {
    assertThat(applicationContext.getBeansOfType(InMemoryUserDetailsManager.class)).isEmpty();
  }
}
