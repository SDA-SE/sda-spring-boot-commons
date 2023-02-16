/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.cacertificates;

import static org.assertj.core.api.Assertions.assertThat;

import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = TestApp.class)
class DefaultSSLContextTest {

  @Autowired SSLContext sslContext;

  @Test
  void shouldCreateSSlContextBean() {
    assertThat(sslContext).isNotNull();
  }
}
