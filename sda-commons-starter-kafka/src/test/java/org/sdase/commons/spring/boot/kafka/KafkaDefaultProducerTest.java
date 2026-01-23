/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestModel;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "management.server.port=0",
      "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
    })
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"port=0"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ExtendWith(MockitoExtension.class)
class KafkaDefaultProducerTest {

  @Autowired private KafkaTestProducer testee;
  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Value("${app.kafka.producer.topic}")
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
                        KafkaTestUtil.getNextRecord(
                                topic,
                                KafkaTestUtil.createTestConsumer(
                                    topic,
                                    embeddedKafkaBroker,
                                    new JacksonJsonDeserializer<>().trustedPackages("*")))
                            .value())
                    .usingRecursiveComparison()
                    .isEqualTo(expectedMessage));
  }
}
