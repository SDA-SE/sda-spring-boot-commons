/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.kafka;

import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

public class KafkaTestUtil {

  private KafkaTestUtil() {}

  public static ConsumerRecord<String, ?> getNextRecord(
      String topic, KafkaConsumer<String, ?> consumer) {
    return KafkaTestUtils.getSingleRecord(consumer, topic);
  }

  public static KafkaConsumer<String, ?> createTestConsumer(
      String topic, EmbeddedKafkaBroker embeddedKafkaBroker, Deserializer<?> deserializer) {

    KafkaConsumer<String, ?> consumer =
        new KafkaConsumer<>(
            KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker),
            new StringDeserializer(),
            deserializer);
    consumer.subscribe(List.of(topic));
    return consumer;
  }
}
