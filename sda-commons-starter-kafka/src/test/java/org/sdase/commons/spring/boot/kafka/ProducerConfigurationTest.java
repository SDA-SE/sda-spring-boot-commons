/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.kafka.common.errors.SerializationException;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;

@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "management.server.port=0",
      "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
      "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer"
    })
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"port=0"})
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class ProducerConfigurationTest {

  @Autowired KafkaTemplate<String, KafkaTestModel> sdaGenericTemplate;

  @Autowired
  @Qualifier("kafkaByteArrayDltTemplate")
  KafkaTemplate<String, ?> dltTemplate;

  @Value("${app.kafka.consumer.retry-and-dlt.topic}")
  private String topic;

  @Test
  void useDifferentProducerFactories() {
    ProducerFactory<String, ?> producerFactory = sdaGenericTemplate.getProducerFactory();
    ProducerFactory<String, ?> dltProducerFactory = dltTemplate.getProducerFactory();

    assertThat(producerFactory).isNotEqualTo(dltProducerFactory);
  }

  @Test
  void allowValueSerializerOverride() {

    //    indirectly, test if the value serializer is set not to json serializer and throws
    // exception
    assertThrows(
        SerializationException.class,
        () -> {
          KafkaTestModel message = new KafkaTestModel().setCheckString("CHECK").setCheckInt(null);
          sdaGenericTemplate.send(topic, message);
        });
  }
}
