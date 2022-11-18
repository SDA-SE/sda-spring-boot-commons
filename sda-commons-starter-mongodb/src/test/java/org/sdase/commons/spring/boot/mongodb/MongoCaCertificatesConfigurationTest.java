/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mongodb;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.client.MongoClient;
import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest()
class MongoCaCertificatesConfigurationTest {

  @Autowired MongoClient mongoClient;

  @Autowired SSLContext sslContext;

  @Test
  void shouldUseTheCreatedSslContext() {
    assertThat(mongoClient)
        .isNotNull()
        .extracting("settings.sslSettings.context")
        .isNotNull()
        .isEqualTo(sslContext);
  }
}
