/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.cloudevents;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.cloudevents.app.KafkaTestApp;
import org.sdase.commons.spring.boot.cloudevents.app.partner.PartnerCreatedEvent;
import org.sdase.commons.spring.boot.cloudevents.app.partner.PartnerCreatedMessageProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(
    classes = KafkaTestApp.class,
    properties = {
      "management.server.port=0",
      "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
      "spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JacksonJsonSerializer"
    })
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"port=9092"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class PartnerCloudEventTest {

  @Autowired private PartnerCreatedMessageProducer partnerCreatedMessageProducer;

  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  private static final String TOPIC = "test-topic";

  private BlockingQueue<ConsumerRecord<String, PartnerCreatedEvent>> consumerRecords;

  private KafkaMessageListenerContainer<String, PartnerCreatedEvent> container;

  @BeforeEach
  void setUp() {

    Map<String, Object> configs =
        new HashMap<>(KafkaTestUtils.consumerProps(embeddedKafkaBroker, "consumer", false));

    JacksonJsonDeserializer<PartnerCreatedEvent> partnerCreatedEventJsonDeserializer =
        new JacksonJsonDeserializer<>();
    partnerCreatedEventJsonDeserializer.addTrustedPackages(
        "org.sdase.commons.spring.boot.cloudevents.app.partner");

    DefaultKafkaConsumerFactory<String, PartnerCreatedEvent> consumerFactory =
        new DefaultKafkaConsumerFactory<>(
            configs, new StringDeserializer(), partnerCreatedEventJsonDeserializer);

    ContainerProperties containerProperties = new ContainerProperties(TOPIC);
    container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
    consumerRecords = new LinkedBlockingQueue<>();
    container.setupMessageListener(
        (MessageListener<String, PartnerCreatedEvent>) consumerRecords::add);
    container.start();
    ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
  }

  @AfterEach
  void tearDown() {
    container.stop();
  }

  @Test
  void shouldProduceAndConsumePartnerCloudEvent()
      throws ExecutionException, InterruptedException, TimeoutException {

    String partnerId = UUID.randomUUID().toString();

    PartnerCreatedEvent cloudEvent =
        (PartnerCreatedEvent)
            new PartnerCreatedEvent()
                .setData(new PartnerCreatedEvent.PartnerCreated(partnerId))
                .setSubject(partnerId);

    partnerCreatedMessageProducer.send(TOPIC, cloudEvent);

    ConsumerRecord<String, PartnerCreatedEvent> poll = consumerRecords.poll(10, TimeUnit.SECONDS);
    PartnerCreatedEvent value = poll.value();
    assertThat(value.getData().id()).isEqualTo(partnerId);
  }
}
