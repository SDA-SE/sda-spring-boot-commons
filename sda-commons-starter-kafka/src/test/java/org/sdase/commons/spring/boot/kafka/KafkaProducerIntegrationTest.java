/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

// ATTENTION: The source of this class is included in the public documentation.

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestModel;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {"management.server.port=0"})
@EmbeddedKafka
class KafkaProducerIntegrationTest {

  @Autowired EmbeddedKafkaBroker kafkaEmbedded;

  @Autowired KafkaTestProducer kafkaTestProducer;

  @Value("${app.kafka.producer.topic}") // as defined for the producer
  String topic;

  @Test
  void shouldProduceMessage() throws ExecutionException, InterruptedException, TimeoutException {
    KafkaTestModel given = new KafkaTestModel().setCheckInt(1).setCheckString("Hello World!");

    kafkaTestProducer.send(given);

    var actualRecords = consumeRecords(topic);
    assertThat(actualRecords)
        .hasSize(1)
        .first()
        .extracting(ConsumerRecord::value)
        .asString()
        .contains("Hello World!");
  }

  ConsumerRecords<String, String> consumeRecords(String topic) {
    try (var testConsumer = createTestConsumer(topic)) {
      return testConsumer.poll(Duration.ofSeconds(10));
    }
  }

  KafkaConsumer<String, String> createTestConsumer(String topic) {
    KafkaConsumer<String, String> consumer =
        new KafkaConsumer<>(
            KafkaTestUtils.consumerProps(kafkaEmbedded.getBrokersAsString(), "test-consumer", true),
            new StringDeserializer(),
            new StringDeserializer());
    consumer.subscribe(Set.of(topic));
    return consumer;
  }
}
