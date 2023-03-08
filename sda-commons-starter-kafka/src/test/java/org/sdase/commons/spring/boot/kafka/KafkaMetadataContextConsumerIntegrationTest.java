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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestModel;
import org.sdase.commons.spring.boot.kafka.test.MetadataCollector;
import org.sdase.commons.spring.boot.web.metadata.MetadataContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SetSystemProperty(key = "METADATA_FIELDS", value = "tenant-id")
@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
class KafkaMetadataContextConsumerIntegrationTest {

  @Autowired KafkaTemplate<String, KafkaTestModel> kafkaMetadataTemplate;

  @Autowired MetadataCollector metadataCollector;

  @Value("${app.kafka.consumer.metadata.topic}")
  private String topic;

  @BeforeEach
  void clearCollectedContext() {
    metadataCollector.clearLastCollectedContext();
  }

  @Test
  void shouldCreateMetadataContextFromRecord() {
    kafkaMetadataTemplate.send(
        new ProducerRecord<>(
            topic,
            0,
            UUID.randomUUID().toString(),
            new KafkaTestModel().setCheckInt(1).setCheckString("CHECK"),
            new RecordHeaders().add("tenant-id", "t-1".getBytes())));

    await()
        .untilAsserted(() -> assertThat(metadataCollector.getLastCollectedContext()).isNotNull());

    assertThat(metadataCollector.getLastCollectedContext())
        .containsEntry("tenant-id", List.of("t-1"));
  }

  @Test
  void shouldClearMetadataContextAfterHandlingMessage() {
    kafkaMetadataTemplate.send(
        new ProducerRecord<>(
            topic,
            0,
            UUID.randomUUID().toString(),
            new KafkaTestModel().setCheckInt(1).setCheckString("CHECK"),
            new RecordHeaders().add("tenant-id", "t-1".getBytes())));

    await()
        .untilAsserted(() -> assertThat(metadataCollector.getLastCollectedContext()).isNotNull());

    assertThat(MetadataContext.detachedCurrent()).isEmpty();
  }

  @Test
  void shouldNormalizeReceivedMetadataContext() {
    kafkaMetadataTemplate.send(
        new ProducerRecord<>(
            topic,
            0,
            UUID.randomUUID().toString(),
            new KafkaTestModel().setCheckInt(1).setCheckString("CHECK"),
            buildHeaders(Map.of("tenant-id", List.of("t-1", "  ", " t-2 ")))));

    await()
        .untilAsserted(() -> assertThat(metadataCollector.getLastCollectedContext()).isNotNull());

    assertThat(metadataCollector.getLastCollectedContext())
        .containsEntry("tenant-id", List.of("t-1", "t-2"));
  }

  @Test
  void shouldNotReceiveUnknownFields() {
    kafkaMetadataTemplate.send(
        new ProducerRecord<>(
            topic,
            0,
            UUID.randomUUID().toString(),
            new KafkaTestModel().setCheckInt(1).setCheckString("CHECK"),
            buildHeaders(Map.of("tenant-id", List.of("t-1"), "shop-id", List.of("s-1")))));

    await()
        .untilAsserted(() -> assertThat(metadataCollector.getLastCollectedContext()).isNotNull());

    assertThat(metadataCollector.getLastCollectedContext())
        .containsEntry("tenant-id", List.of("t-1"))
        .doesNotContainEntry("shop-id", List.of("s-1"));
  }

  private RecordHeaders buildHeaders(Map<String, List<String>> metadata) {
    var headers = new RecordHeaders();
    metadata.forEach(
        (key, value) ->
            value.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .map(v -> v.getBytes(StandardCharsets.UTF_8))
                .forEach(v -> headers.add(key, v)));

    return headers;
  }
}
