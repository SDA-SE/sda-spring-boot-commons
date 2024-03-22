/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

// ATTENTION: The source of this class is included in the public documentation.

import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.verify;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.sdase.commons.spring.boot.kafka.test.SomeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;

@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {"management.server.port=0"})
@EmbeddedKafka
class KafkaConsumerIntegrationTest {

  @Autowired KafkaTemplate<String, Object> kafkaTemplate;

  @SpyBean SomeService someService;

  @Value("${app.kafka.consumer.topic}") // same as configured for the consumer
  private String topic;

  @Test
  void shouldDoSomethingWithMessage() {
    String givenMessageKey = "some-key";
    var givenMessageValue = Map.of("property", "value");

    kafkaTemplate.send(topic, givenMessageKey, givenMessageValue);

    // verify that a repository saved data derived from the message, an external service is
    // called or whatever should happen when the message is received
    await().untilAsserted(() -> verify(someService).didTheJob(givenMessageValue));
  }
}
