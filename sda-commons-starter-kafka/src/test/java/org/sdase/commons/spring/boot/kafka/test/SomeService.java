/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * This is just an example service to document Kafka consumer integration testing in {@code
 * org.sdase.commons.spring.boot.kafka.KafkaConsumerIntegrationTest}.
 */
@Component
public class SomeService {

  private static final Logger LOG = LoggerFactory.getLogger(SomeService.class);

  public void didTheJob(Object message) {
    LOG.debug("Received {}", message);
  }
}
