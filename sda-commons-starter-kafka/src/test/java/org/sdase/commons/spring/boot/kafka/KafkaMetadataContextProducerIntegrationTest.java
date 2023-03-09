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

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.sdase.commons.spring.boot.metadata.context.DetachedMetadataContext;
import org.sdase.commons.spring.boot.metadata.context.MetadataContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

@SetSystemProperty(key = "METADATA_FIELDS", value = "tenant-id")
@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
@DirtiesContext(methodMode = DirtiesContext.MethodMode.BEFORE_METHOD)
class KafkaMetadataContextProducerIntegrationTest {

  @Autowired private KafkaTemplate<String, String> kafkaMetadataTemplate;
  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Value("${app.kafka.producer.topic}")
  private String topic;

  @BeforeEach
  void createTopicIfMissing() {
    if (!embeddedKafkaBroker.getTopics().contains(topic)) {
      embeddedKafkaBroker.addTopics(topic);
    }
  }

  @BeforeEach
  @AfterEach
  void clearMetadata() {
    MetadataContext.createContext(new DetachedMetadataContext());
  }

  @Test
  void shouldSendMetadataContext() {
    DetachedMetadataContext given = new DetachedMetadataContext();
    given.put("tenant-id", List.of("t-1"));
    MetadataContext.createContext(given);

    KafkaConsumer<String, ?> consumer =
        new KafkaConsumer<>(
            KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "true", embeddedKafkaBroker),
            new StringDeserializer(),
            new StringDeserializer());
    consumer.subscribe(List.of(topic));

    kafkaMetadataTemplate.send(topic, "key", "value");

    var lastRecord = getLastRecord(KafkaTestUtils.getRecords(consumer).iterator());
    assertThat(lastRecord.headers())
        .extracting(Header::key, Header::value)
        .contains(tuple("tenant-id", "t-1".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void shouldKeepDefinedHeaders() {
    DetachedMetadataContext given = new DetachedMetadataContext();
    given.put("tenant-id", List.of("t-1"));
    MetadataContext.createContext(given);
    var headers = new RecordHeaders();
    headers.add("custom", "custom-value".getBytes(StandardCharsets.UTF_8));

    KafkaConsumer<String, ?> consumer =
        new KafkaConsumer<>(
            KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "true", embeddedKafkaBroker),
            new StringDeserializer(),
            new StringDeserializer());
    consumer.subscribe(List.of(topic));

    kafkaMetadataTemplate.send(new ProducerRecord<>(topic, 0, "key", "value", headers));

    var lastRecord = getLastRecord(KafkaTestUtils.getRecords(consumer).iterator());
    assertThat(lastRecord.headers())
        .extracting(Header::key, Header::value)
        .contains(
            tuple("tenant-id", "t-1".getBytes(StandardCharsets.UTF_8)),
            tuple("custom", "custom-value".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void shouldPreferHeadersFromMetadataContext() {
    DetachedMetadataContext given = new DetachedMetadataContext();
    given.put("tenant-id", List.of("t-1"));
    MetadataContext.createContext(given);
    var headers = new RecordHeaders();
    headers.add("tenant-id", "custom-tenant".getBytes(StandardCharsets.UTF_8));

    KafkaConsumer<String, ?> consumer =
        new KafkaConsumer<>(
            KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "true", embeddedKafkaBroker),
            new StringDeserializer(),
            new StringDeserializer());
    consumer.subscribe(List.of(topic));

    kafkaMetadataTemplate.send(new ProducerRecord<>(topic, 0, "key", "value", headers));

    var lastRecord = getLastRecord(KafkaTestUtils.getRecords(consumer).iterator());
    assertThat(lastRecord.headers())
        .extracting(Header::key, Header::value)
        .contains(tuple("tenant-id", "t-1".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void shouldSendMetadataContextNormalized() {
    DetachedMetadataContext given = new DetachedMetadataContext();
    given.put("tenant-id", List.of("t-1", "  ", " t-2 "));
    MetadataContext.createContext(given);

    KafkaConsumer<String, ?> consumer =
        new KafkaConsumer<>(
            KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "true", embeddedKafkaBroker),
            new StringDeserializer(),
            new StringDeserializer());
    consumer.subscribe(List.of(topic));

    kafkaMetadataTemplate.send(topic, UUID.randomUUID().toString(), "value");

    var lastRecord = getLastRecord(KafkaTestUtils.getRecords(consumer).iterator());
    assertThat(lastRecord.headers())
        .extracting(Header::key, Header::value)
        .contains(
            tuple("tenant-id", "t-1".getBytes(StandardCharsets.UTF_8)),
            tuple("tenant-id", "t-2".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  void shouldNotSendUnknownFieldsOfMetadataContext() {
    DetachedMetadataContext given = new DetachedMetadataContext();
    given.put("shop-id", List.of("s-1"));
    MetadataContext.createContext(given);

    KafkaConsumer<String, ?> consumer =
        new KafkaConsumer<>(
            KafkaTestUtils.consumerProps(UUID.randomUUID().toString(), "true", embeddedKafkaBroker),
            new StringDeserializer(),
            new StringDeserializer());
    consumer.subscribe(List.of(topic));

    kafkaMetadataTemplate.send(topic, "key", "value");

    var lastRecord = getLastRecord(KafkaTestUtils.getRecords(consumer).iterator());
    assertThat(lastRecord.headers())
        .extracting(Header::key, Header::value)
        .isNotEmpty()
        .doesNotContain(tuple("shop-id", "s-1".getBytes(StandardCharsets.UTF_8)));
  }

  private ConsumerRecord<String, ?> getLastRecord(
      Iterator<? extends ConsumerRecord<String, ?>> iterator) {
    ConsumerRecord<String, ?> result = null;
    while (iterator.hasNext()) {
      result = iterator.next();
    }
    return result;
  }
}
