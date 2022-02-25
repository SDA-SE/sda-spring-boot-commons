/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.kafka;

import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
@PropertySource("classpath:/org/sdase/commons/spring/kafka/producer.properties")
public class SdaKafkaProducerConfiguration {

  private final KafkaProperties kafkaProperties;

  public SdaKafkaProducerConfiguration(KafkaProperties kafkaProperties) {
    this.kafkaProperties = kafkaProperties;
  }

  @Bean
  public ProducerFactory<String, ?> producerFactory() { // NOSONAR
    return new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
  }

  @Bean
  public KafkaTemplate<String, ?> kafkaTemplate() { // NOSONAR
    return new KafkaTemplate<>(producerFactory());
  }
}
