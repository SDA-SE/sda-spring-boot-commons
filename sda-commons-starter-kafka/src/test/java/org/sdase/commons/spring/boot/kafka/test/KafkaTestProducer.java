/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka.test;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaTestProducer {

  private final String topicName;
  private final KafkaTemplate<String, KafkaTestModel> kafkaTemplate;

  public KafkaTestProducer(
      @Value("${app.kafka.producer.topic}") String topicName,
      KafkaTemplate<String, KafkaTestModel> kafkaTemplate) {
    this.topicName = topicName;
    this.kafkaTemplate = kafkaTemplate;
  }

  public void send(KafkaTestModel model)
      throws ExecutionException, InterruptedException, TimeoutException {
    kafkaTemplate.send(topicName, UUID.randomUUID().toString(), model).get(10, TimeUnit.SECONDS);
  }
}
