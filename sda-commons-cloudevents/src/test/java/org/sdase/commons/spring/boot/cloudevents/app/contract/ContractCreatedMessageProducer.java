/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.cloudevents.app.contract;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ContractCreatedMessageProducer {

  private final KafkaTemplate<String, ContractCreatedEvent> kafkaTemplate;

  public ContractCreatedMessageProducer(KafkaTemplate<String, ContractCreatedEvent> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void send(String topicName, ContractCreatedEvent contractCreatedEvent)
      throws ExecutionException, InterruptedException, TimeoutException {

    kafkaTemplate
        .send(topicName, UUID.randomUUID().toString(), contractCreatedEvent)
        .get(10, TimeUnit.SECONDS);
  }
}
