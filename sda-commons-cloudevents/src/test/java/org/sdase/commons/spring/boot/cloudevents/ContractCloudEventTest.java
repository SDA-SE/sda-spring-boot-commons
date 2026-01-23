/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.cloudevents;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
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
import org.sdase.commons.spring.boot.cloudevents.app.contract.ContractCreatedEvent;
import org.sdase.commons.spring.boot.cloudevents.app.contract.ContractCreatedMessageProducer;
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
class ContractCloudEventTest {

  @Autowired private ContractCreatedMessageProducer contractCreatedMessageProducer;

  @Autowired private EmbeddedKafkaBroker embeddedKafkaBroker;

  private String testTopic = "test-topic";

  private BlockingQueue<ConsumerRecord<String, ContractCreatedEvent>> consumerRecords;

  private KafkaMessageListenerContainer<String, ContractCreatedEvent> container;

  @BeforeEach
  void setUp() {

    Map<String, Object> configs =
        new HashMap<>(KafkaTestUtils.consumerProps(embeddedKafkaBroker, "consumer", false));

    JacksonJsonDeserializer<ContractCreatedEvent> jsonDeserializer =
        new JacksonJsonDeserializer<>();
    jsonDeserializer.addTrustedPackages("org.sdase.commons.spring.boot.cloudevents.app.contract");

    DefaultKafkaConsumerFactory<String, ContractCreatedEvent> consumerFactory =
        new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(), jsonDeserializer);

    ContainerProperties containerProperties = new ContainerProperties(testTopic);
    container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
    consumerRecords = new LinkedBlockingQueue<>();
    container.setupMessageListener(
        (MessageListener<String, ContractCreatedEvent>) consumerRecords::add);
    container.start();
    ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
  }

  @AfterEach
  void tearDown() {
    container.stop();
  }

  @Test
  void shouldProduceAndConsumeContractCloudEvent()
      throws ExecutionException, InterruptedException, TimeoutException {

    String partnerId = UUID.randomUUID().toString();
    String contractId = UUID.randomUUID().toString();

    ContractCreatedEvent cloudEvent =
        (ContractCreatedEvent)
            new ContractCreatedEvent()
                .setSource(
                    URI.create(
                        "/SDA-SE/insurance-contract/insurance-contract-stack/insurance-contract-service"))
                .setSubject(contractId)
                .setType("com.sdase.contract.foo.contract.created")
                .setData(new ContractCreatedEvent.ContractCreated(contractId, partnerId));

    contractCreatedMessageProducer.send(testTopic, cloudEvent);

    ConsumerRecord<String, ContractCreatedEvent> poll = consumerRecords.poll(10, TimeUnit.SECONDS);
    ContractCreatedEvent value = poll.value();

    assertThat(value.getData().getContractId()).isEqualTo(contractId);
    assertThat(value.getData().getPartnerId()).isEqualTo(partnerId);
  }
}
