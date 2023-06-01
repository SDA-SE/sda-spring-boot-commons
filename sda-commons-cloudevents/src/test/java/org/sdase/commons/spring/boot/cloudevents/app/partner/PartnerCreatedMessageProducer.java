/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.cloudevents.app.partner;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PartnerCreatedMessageProducer {

  private final KafkaTemplate<String, PartnerCreatedEvent> kafkaTemplate;

  public PartnerCreatedMessageProducer(KafkaTemplate<String, PartnerCreatedEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void send(String topicName, PartnerCreatedEvent cloudEvent)
      throws ExecutionException, InterruptedException, TimeoutException {

    kafkaTemplate
        .send(topicName, UUID.randomUUID().toString(), cloudEvent)
        .get(10, TimeUnit.SECONDS);
  }
}
