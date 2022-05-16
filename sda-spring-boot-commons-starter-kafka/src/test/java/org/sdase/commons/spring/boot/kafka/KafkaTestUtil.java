/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.kafka;

import java.util.List;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

public class KafkaTestUtil {

  private KafkaTestUtil() {}

  public static ConsumerRecord<String, Object> getNextRecord(
      String topic, KafkaConsumer<String, Object> consumer) {
    return KafkaTestUtils.getSingleRecord(consumer, topic);
  }

  public static KafkaConsumer<String, Object> createTestConsumer(
      String topic, EmbeddedKafkaBroker embeddedKafkaBroker) {

    KafkaConsumer<String, Object> consumer =
        new KafkaConsumer<>(
            KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker),
            new StringDeserializer(),
            new JsonDeserializer<Object>().trustedPackages("*"));
    consumer.subscribe(List.of(topic));
    return consumer;
  }
}
