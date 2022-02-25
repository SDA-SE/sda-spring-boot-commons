/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.kafka;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.verification.Timeout;
import org.sdase.commons.spring.kafka.test.KafkaTestApp;
import org.sdase.commons.spring.kafka.test.KafkaTestListener.ListenerCheck;
import org.sdase.commons.spring.kafka.test.KafkaTestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "app.kafka.consumer.test-topic=test-topic-consumer",
      "app.kafka.producer.test-topic=test-topic-producer",
      "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
    })
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class KafkaDefaultConsumerTest {

  @Autowired KafkaTemplate<String, KafkaTestModel> kafkaTemplate;

  @Autowired ObjectMapper objectMapper;

  @MockBean ListenerCheck listenerCheck;

  @Value("${app.kafka.consumer.test-topic}")
  private String topic;

  @Test
  void shouldReceiveAndDeserializeToJson() throws Exception {
    kafkaTemplate.send(topic, new KafkaTestModel().setCheckString("CHECK").setCheckInt(1));

    verify(listenerCheck, timeout(3000)).check("CHECK");
  }

  @Test
  void shouldNotReceiveInvalidModel() throws Exception {
    kafkaTemplate.send(topic, new KafkaTestModel().setCheckString("CHECK").setCheckInt(null));

    verify(listenerCheck, new Timeout(2000, never())).check("CHECK");
  }
}
