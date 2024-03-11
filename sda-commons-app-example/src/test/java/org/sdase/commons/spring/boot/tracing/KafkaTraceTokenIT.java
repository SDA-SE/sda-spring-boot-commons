/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.tracing;

import static java.util.Map.entry;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.kafka.TraceTokenRecordInterceptor;
import org.sdase.commons.spring.boot.tracing.app.KafkaTraceTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = KafkaTraceTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
@AutoConfigureWireMock(port = 0)
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class KafkaTraceTokenIT {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  private TraceTokenRecordInterceptor traceTokenRecordInterceptor;

  private KafkaMessageListenerContainer<String, String> listenerContainer;

  private BlockingQueue<ConsumerRecord<String, String>> consumerRecords;

  private Map<String, String> mdcContextWhileMessageIsConsumed;

  @Value("${app.kafka.producer.topic}")
  private String topic;

  @BeforeEach
  void setUp() {

    WireMock.reset();

    traceTokenRecordInterceptor = new TraceTokenRecordInterceptor();

    mdcContextWhileMessageIsConsumed = new HashMap<>();

    Map<String, Object> configs =
        new HashMap<>(KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker));

    DefaultKafkaConsumerFactory<String, String> consumerFactory =
        new DefaultKafkaConsumerFactory<>(
            configs, new StringDeserializer(), new StringDeserializer());

    ContainerProperties containerProperties = new ContainerProperties(topic);
    listenerContainer = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

    consumerRecords = new LinkedBlockingQueue<>();
    listenerContainer.setupMessageListener(
        new KafkaTraceTestMessageListener<>(consumerRecords, mdcContextWhileMessageIsConsumed));
    listenerContainer.setRecordInterceptor(traceTokenRecordInterceptor);
    listenerContainer.start();
    ContainerTestUtils.waitForAssignment(
        listenerContainer, embeddedKafkaBroker.getPartitionsPerTopic());
  }

  @Test
  void shouldPreserveTraceTokenIfPresent() throws InterruptedException {
    // given
    var headers = new HttpHeaders();
    headers.set("Trace-Token", "pre-defined-trace-token");
    // when
    ResponseEntity<String> responseEntity =
        executeRequestWithHeader("/api/createEvent?user=user_1", port, headers);

    ConsumerRecord<String, String> poll = consumerRecords.poll(10, TimeUnit.SECONDS);

    Optional<Header> headerParentTraceToken =
        Arrays.stream(poll.headers().toArray())
            .filter(header -> header.key().equals("Parent-Trace-Token"))
            .findFirst();

    //    checks MDC while a message is consumed
    assertThat(mdcContextWhileMessageIsConsumed)
        .contains(entry("Parent-Trace-Token", "pre-defined-trace-token"));

    assertTrue(headerParentTraceToken.isPresent());
    assertThat(new String(headerParentTraceToken.get().value()))
        .isEqualTo("pre-defined-trace-token");

    //    checks that MDC is cleaned up after a message consumed
    String mdcTraceTokenAfterMessageConsumed = MDC.get("Parent-Trace-Token");
    assertThat(mdcTraceTokenAfterMessageConsumed).isNull();
  }

  private ResponseEntity<String> executeRequestWithHeader(
      String path, int port, HttpHeaders headers) {
    return client.exchange(
        String.format("http://localhost:%d%s", port, path),
        HttpMethod.GET,
        new HttpEntity<>(headers),
        String.class);
  }

  class KafkaTraceTestMessageListener<K, V> implements MessageListener<K, V> {

    private BlockingQueue<ConsumerRecord<K, V>> consumerRecords;

    private Map<String, String> mdcContextWhileMessageIsConsumed;

    public KafkaTraceTestMessageListener(
        BlockingQueue<ConsumerRecord<K, V>> consumerRecords,
        Map<String, String> mdcContextWhileMessageIsConsumed) {
      this.consumerRecords = consumerRecords;
      this.mdcContextWhileMessageIsConsumed = mdcContextWhileMessageIsConsumed;
    }

    @Override
    public void onMessage(ConsumerRecord<K, V> data) {
      mdcContextWhileMessageIsConsumed.putAll(MDC.getCopyOfContextMap());
      consumerRecords.add(data);
    }
  }
}
