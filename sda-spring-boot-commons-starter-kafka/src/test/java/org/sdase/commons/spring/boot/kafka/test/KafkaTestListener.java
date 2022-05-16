/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.kafka.test;

import javax.validation.Valid;
import org.sdase.commons.spring.boot.kafka.NotRetryableKafkaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaTestListener {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaTestListener.class);
  private final ListenerCheck listenerCheck;

  public KafkaTestListener(ListenerCheck listenerCheck) {
    this.listenerCheck = listenerCheck;
  }

  @KafkaListener(
      topics = "${app.kafka.consumer.test-topic}",
      containerFactory = "retryAndLogKafkaListenerContainerFactory")
  public void receive(@Payload @Valid KafkaTestModel kafkaTestModel) {
    listenerCheck.check(kafkaTestModel.getCheckString());
  }

  @KafkaListener(
      topics = "${app.kafka.consumer.dld-topic}",
      containerFactory = "retryAndDldKafkaListenerContainerFactory")
  public void receiveForDld(@Payload @Valid KafkaTestModel kafkaTestModel) {
    if (kafkaTestModel.isThrowNotRetryableException()) {
      throw new NotRetryableKafkaException();
    }
    listenerCheck.check(kafkaTestModel.getCheckString());
  }

  @Component
  public static class ListenerCheck {
    public void check(String checkString) {
      // DO NOTING
    }
  }
}
