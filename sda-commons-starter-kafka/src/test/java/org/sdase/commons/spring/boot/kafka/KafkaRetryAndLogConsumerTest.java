/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestListener.ListenerCheck;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestModel;
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
    properties = {"spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"})
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.STRICT_STUBS)
class KafkaRetryAndLogConsumerTest {

  @Autowired KafkaTemplate<String, KafkaTestModel> kafkaTemplate;

  @Autowired ObjectMapper objectMapper;

  @MockBean ListenerCheck listenerCheck;

  @Value("${app.kafka.consumer.retry-and-log.topic}")
  private String topic;

  @Test
  void shouldReceiveAndDeserializeToJson() throws Exception {
    kafkaTemplate.send(topic, new KafkaTestModel().setCheckString("CHECK").setCheckInt(1));
    verify(listenerCheck, timeout(5000)).check("CHECK");
  }

  @Test
  void shouldNotReceiveInvalidModel() throws Exception {
    kafkaTemplate.send(topic, new KafkaTestModel().setCheckString("CHECK").setCheckInt(null));
    verify(listenerCheck, timeout(5000)).check("CHECK");
  }

  @Test
  void shouldNeverRetryWithNotRetryableException() throws Exception {
    kafkaTemplate.send(
        topic,
        new KafkaTestModel()
            .setCheckString("CHECK")
            .setCheckInt(1)
            .setThrowNotRetryableException(true));
    verify(listenerCheck, timeout(5000).times(1)).check("CHECK");
  }

  @Test
  void shouldRetryWithForRuntimeException() throws Exception {
    kafkaTemplate.send(
        topic,
        new KafkaTestModel().setCheckString("CHECK").setCheckInt(1).setThrowRuntimeException(true));
    verify(listenerCheck, timeout(5000).times(2)).check("CHECK");
  }
}
