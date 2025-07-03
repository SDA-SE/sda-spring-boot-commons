/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.sdase.commons.spring.boot.kafka.KafkaTestUtil.readValue;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.verification.Timeout;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestListener;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
      "app.kafka.consumer.retry-and-dlt.topic=topic-consumer",
      "management.server.port=0"
    })
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class KafkaDefaultDltPatternTest {

  @Autowired KafkaTemplate<String, KafkaTestModel> kafkaTemplate;

  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  @Autowired ObjectMapper objectMapper;

  @MockitoBean KafkaTestListener.ListenerCheck listenerCheck;

  @Value("${app.kafka.consumer.retry-and-dlt.topic}")
  private String topic;

  private KafkaMessageListenerContainer<String, String> containerDLT;
  private BlockingQueue<ConsumerRecord<String, String>> consumerRecordsDLT;

  @BeforeEach
  void setUp() {

    Map<String, Object> configs =
        new HashMap<>(KafkaTestUtils.consumerProps("consumer", "false", embeddedKafkaBroker));

    DefaultKafkaConsumerFactory<String, String> consumerFactory =
        new DefaultKafkaConsumerFactory<>(
            configs, new StringDeserializer(), new StringDeserializer());

    ContainerProperties containerProperties = new ContainerProperties("dlt-topic-consumer");
    containerDLT = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);

    consumerRecordsDLT = new LinkedBlockingQueue<>();
    containerDLT.setupMessageListener((MessageListener<String, String>) consumerRecordsDLT::add);
    containerDLT.start();
    ContainerTestUtils.waitForAssignment(containerDLT, embeddedKafkaBroker.getPartitionsPerTopic());
  }

  @AfterEach
  void tearDown() {
    containerDLT.stop();
  }

  @Test
  void shouldUseDefaultDltPattern() throws Exception {
    KafkaTestModel message = new KafkaTestModel().setCheckString("CHECK").setCheckInt(null);
    kafkaTemplate.send(topic, message);
    verify(listenerCheck, new Timeout(5000, never())).check("CHECK");

    ConsumerRecord<String, String> pollDLT = consumerRecordsDLT.poll(10, TimeUnit.SECONDS);

    KafkaTestModel messageFromDLT = readValue(pollDLT, objectMapper);
    assertThat(messageFromDLT.getCheckString()).isEqualTo("CHECK");

    Optional<Header> headerExceptionCause =
        Arrays.stream(pollDLT.headers().toArray())
            .filter(header -> header.key().equals("kafka_dlt-exception-cause-fqcn"))
            .findFirst();
    assertThat(new String(headerExceptionCause.get().value()))
        .isEqualTo(MethodArgumentNotValidException.class.getName());
  }
}
