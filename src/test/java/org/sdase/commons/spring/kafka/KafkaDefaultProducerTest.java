/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sdase.commons.spring.kafka.test.KafkaTestApp;
import org.sdase.commons.spring.kafka.test.KafkaTestModel;
import org.sdase.commons.spring.kafka.test.KafkaTestProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "app.kafka.producer.test-topic=test-topic-producer",
      "app.kafka.consumer.test-topic=test-topic-consumer",
      "management.server.port=0",
      "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
    })
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ExtendWith(MockitoExtension.class)
class KafkaDefaultProducerTest {

  @Autowired private KafkaTestProducer testee;
  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Value("${app.kafka.producer.test-topic}")
  private String topic;

  @Test
  void shouldSend() throws ExecutionException, InterruptedException, TimeoutException {
    var expectedMessage = new KafkaTestModel().setCheckInt(1).setCheckString("CHECK");
    testee.send(expectedMessage);
    await()
        .atMost(Duration.ofSeconds(10))
        .pollDelay(Duration.ofMillis(1000))
        .pollInterval(Duration.ofMillis(100))
        .untilAsserted(
            () ->
                assertThat(
                        KafkaTestUtils.getSingleRecord(
                                createTestConsumer(topic, embeddedKafkaBroker), topic)
                            .value())
                    .usingRecursiveComparison()
                    .isEqualTo(expectedMessage));
  }

  private KafkaConsumer<String, KafkaTestModel> createTestConsumer(
      String topic, EmbeddedKafkaBroker embeddedKafkaBroker) {
    KafkaConsumer<String, KafkaTestModel> consumer =
        new KafkaConsumer<>(
            KafkaTestUtils.consumerProps("test-group", "true", embeddedKafkaBroker),
            new StringDeserializer(),
            new JsonDeserializer<KafkaTestModel>().trustedPackages("*"));
    consumer.subscribe(List.of(topic));
    return consumer;
  }
}
