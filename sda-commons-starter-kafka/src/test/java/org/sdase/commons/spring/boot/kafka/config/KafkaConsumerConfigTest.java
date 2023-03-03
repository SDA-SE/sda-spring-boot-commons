/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = KafkaTestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class KafkaConsumerConfigTest {

  @Autowired private KafkaConsumerConfig kafkaConsumerConfig;

  @Test
  void test_configurationAutowiredCorrectly() {
    assertThat(kafkaConsumerConfig)
        .isNotNull()
        .extracting(
            KafkaConsumerConfig::concurrency,
            config -> config.retry().maxRetries(),
            config -> config.retry().backoffMultiplier(),
            config -> config.retry().initialBackoffInterval(),
            config -> config.retry().maxBackoffInterval())
        .contains(1, 2, 2L, 100L, 500L);
  }
}
