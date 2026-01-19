/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.*;
import static org.sdase.commons.spring.boot.kafka.KafkaTestUtil.readValue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.StdIo;
import org.junitpioneer.jupiter.StdOut;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.verification.Timeout;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestListener.ListenerCheck;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
      "sda.kafka.consumer.dlt.pattern="
    })
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class KafkaRetryAndDltConsumerTest {

  @Autowired KafkaTemplate<String, KafkaTestModel> kafkaTemplate;
  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Autowired ObjectMapper objectMapper;

  @MockitoBean ListenerCheck listenerCheck;

  @Value("${app.kafka.consumer.retry-and-dlt.topic}")
  private String topic;

  @Test
  void shouldReceiveAndDeserializeToJson() {
    kafkaTemplate.send(topic, new KafkaTestModel().setCheckString("CHECK").setCheckInt(1));
    verify(listenerCheck, timeout(5000)).check("CHECK");
  }

  @Test
  void shouldNotReceiveInvalidModelButProduceToDLT() {
    KafkaTestModel expectedMessage = new KafkaTestModel().setCheckString("CHECK").setCheckInt(null);
    kafkaTemplate.send(topic, expectedMessage);
    verify(listenerCheck, new Timeout(5000, never())).check("CHECK");

    try (KafkaConsumer<String, ?> testConsumer =
        KafkaTestUtil.createTestConsumer(
            topic + ".DLT", embeddedKafkaBroker, new StringDeserializer())) {
      await()
          .pollDelay(Duration.ofMillis(1000))
          .pollInterval(Duration.ofMillis(100))
          .untilAsserted(
              () ->
                  assertSoftly(
                      s -> {
                        ConsumerRecord<String, ?> nextRecord =
                            KafkaTestUtil.getNextRecord(topic + ".DLT", testConsumer);
                        var actual = readValue(nextRecord, objectMapper);
                        s.assertThat(actual).usingRecursiveComparison().isEqualTo(expectedMessage);
                        s.assertThat(nextRecord.headers())
                            .extracting(Header::key, header -> new String(header.value()))
                            .contains(
                                tuple(
                                    "kafka_dlt-exception-cause-fqcn",
                                    MethodArgumentNotValidException.class.getName()));
                      }));
    }
  }

  @Test
  @StdIo
  void shouldLogOnProduceToDLT(StdOut out) {
    KafkaTestModel expectedMessage = new KafkaTestModel().setCheckString("CHECK").setCheckInt(null);
    kafkaTemplate.send(topic, expectedMessage);
    verify(listenerCheck, new Timeout(5000, never())).check("CHECK");
    try (KafkaConsumer<String, ?> testConsumer =
        KafkaTestUtil.createTestConsumer(
            topic + ".DLT", embeddedKafkaBroker, new StringDeserializer())) {
      await()
          .pollDelay(Duration.ofMillis(1000))
          .pollInterval(Duration.ofMillis(100))
          .untilAsserted(
              () ->
                  assertSoftly(
                      s -> {
                        ConsumerRecord<String, ?> nextRecord =
                            KafkaTestUtil.getNextRecord(topic + ".DLT", testConsumer);
                        var actual = readValue(nextRecord, objectMapper);
                        s.assertThat(actual).usingRecursiveComparison().isEqualTo(expectedMessage);
                        s.assertThat(nextRecord.headers())
                            .extracting(Header::key, header -> new String(header.value()))
                            .contains(
                                tuple(
                                    "kafka_dlt-exception-cause-fqcn",
                                    MethodArgumentNotValidException.class.getName()));
                      }));
    }
    await()
        .atMost(Duration.ofSeconds(10))
        .pollDelay(Duration.ofMillis(1000))
        .pollInterval(Duration.ofMillis(500))
        .untilAsserted(
            () ->
                assertThat(out.capturedLines())
                    .anyMatch(s -> s.contains("MethodArgumentNotValidException")));
  }

  @Test
  void shouldProduceToDLTForNotRetryableKafkaException() {
    KafkaTestModel expectedMessage =
        new KafkaTestModel()
            .setCheckString("CHECK")
            .setCheckInt(1)
            .setThrowNotRetryableException(true);
    kafkaTemplate.send(topic, expectedMessage);

    verify(listenerCheck, timeout(5000).times(1)).check("CHECK");

    try (KafkaConsumer<String, ?> testConsumer =
        KafkaTestUtil.createTestConsumer(
            topic + ".DLT", embeddedKafkaBroker, new StringDeserializer())) {
      await()
          .pollDelay(Duration.ofMillis(1000))
          .pollInterval(Duration.ofMillis(100))
          .untilAsserted(
              () ->
                  assertSoftly(
                      s -> {
                        ConsumerRecord<String, ?> nextRecord =
                            KafkaTestUtil.getNextRecord(topic + ".DLT", testConsumer);
                        var actual = readValue(nextRecord, objectMapper);
                        s.assertThat(actual).usingRecursiveComparison().isEqualTo(expectedMessage);
                        s.assertThat(nextRecord.headers())
                            .extracting(Header::key, header -> new String(header.value()))
                            .contains(
                                tuple(
                                    "kafka_dlt-exception-cause-fqcn",
                                    NotRetryableKafkaException.class.getName()));
                      }));
    }
  }

  @Test
  void shouldProduceToDLTForRuntimeException() {
    KafkaTestModel expectedMessage =
        new KafkaTestModel().setCheckString("CHECK").setCheckInt(1).setThrowRuntimeException(true);
    kafkaTemplate.send(topic, expectedMessage);

    verify(listenerCheck, timeout(5000).times(2)).check("CHECK");

    try (KafkaConsumer<String, ?> testConsumer =
        KafkaTestUtil.createTestConsumer(
            topic + ".DLT", embeddedKafkaBroker, new StringDeserializer())) {
      await()
          .pollDelay(Duration.ofMillis(1000))
          .pollInterval(Duration.ofMillis(100))
          .untilAsserted(
              () ->
                  assertSoftly(
                      s -> {
                        ConsumerRecord<String, ?> nextRecord =
                            KafkaTestUtil.getNextRecord(topic + ".DLT", testConsumer);
                        var actual = readValue(nextRecord, objectMapper);
                        s.assertThat(actual).usingRecursiveComparison().isEqualTo(expectedMessage);
                        s.assertThat(nextRecord.headers())
                            .extracting(Header::key, header -> new String(header.value()))
                            .contains(
                                tuple(
                                    "kafka_dlt-exception-cause-fqcn",
                                    RuntimeException.class.getName()));
                      }));
    }
  }
}
