/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.kafka;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@PropertySource("classpath:/org/sdase/commons/spring/boot/kafka/producer.properties")
public class SdaKafkaProducerConfiguration {

  @Bean
  public KafkaTemplate<String, ?> kafkaTemplate( // NOSONAR
      ProducerFactory<String, ?> producerFactory) {
    return new KafkaTemplate<>(producerFactory);
  }
}
