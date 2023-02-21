/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import static org.assertj.core.api.Assertions.tuple;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
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

  @MockBean ListenerCheck listenerCheck;

  @Value("${app.kafka.consumer.retry-and-dlt.topic}")
  private String topic;

  @Test
  void shouldReceiveAndDeserializeToJson() throws Exception {
    kafkaTemplate.send(topic, new KafkaTestModel().setCheckString("CHECK").setCheckInt(1));
    verify(listenerCheck, timeout(3000)).check("CHECK");
  }

  @Test
  void shouldNotReceiveInvalidModelButProduceToDLT() throws Exception {
    KafkaTestModel expectedMessage = new KafkaTestModel().setCheckString("CHECK").setCheckInt(null);
    kafkaTemplate.send(topic, expectedMessage);
    verify(listenerCheck, new Timeout(2000, never())).check("CHECK");

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
                        var actual = readValue(nextRecord);
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

  private KafkaTestModel readValue(ConsumerRecord<String, ?> nextRecord) {
    try {
      return objectMapper.readValue((String) nextRecord.value(), KafkaTestModel.class);
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void shouldProduceToDLTForNotRetryableKafkaException() throws Exception {
    KafkaTestModel expectedMessage =
        new KafkaTestModel()
            .setCheckString("CHECK")
            .setCheckInt(1)
            .setThrowNotRetryableException(true);
    kafkaTemplate.send(topic, expectedMessage);

    verify(listenerCheck, timeout(2000).times(1)).check("CHECK");

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
                        var actual = readValue(nextRecord);
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
  void shouldProduceToDLTForRuntimeException() throws Exception {
    KafkaTestModel expectedMessage =
        new KafkaTestModel().setCheckString("CHECK").setCheckInt(1).setThrowRuntimeException(true);
    kafkaTemplate.send(topic, expectedMessage);

    verify(listenerCheck, timeout(4000).times(2)).check("CHECK");

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
                        var actual = readValue(nextRecord);
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
