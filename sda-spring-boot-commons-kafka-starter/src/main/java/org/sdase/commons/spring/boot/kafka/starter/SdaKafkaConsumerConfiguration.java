/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.kafka.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.SeekToCurrentErrorHandler;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@PropertySource("classpath:/org/sdase/commons/spring/boot/kafka/starter/consumer.properties")
public class SdaKafkaConsumerConfiguration implements KafkaListenerConfigurer {

  private final KafkaProperties kafkaProperties;

  private final LocalValidatorFactoryBean validator;
  private final ObjectMapper objectMapper;

  public SdaKafkaConsumerConfiguration(
      KafkaProperties kafkaProperties,
      LocalValidatorFactoryBean validator,
      ObjectMapper objectMapper) {
    this.kafkaProperties = kafkaProperties;
    this.validator = validator;
    this.objectMapper = objectMapper;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object>
      retryAndLogKafkaListenerContainerFactory() {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(
        new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties()));
    factory.setMessageConverter(new ByteArrayJsonMessageConverter(objectMapper));
    // Please note that ConversionExceptions like mapping exception won't be retried and directly
    // logged to error. We may add some specific handling like DeadLetter topics etc.
    factory.setErrorHandler(new SeekToCurrentErrorHandler());
    factory.getContainerProperties().setAckMode(AckMode.RECORD);
    return factory;
  }

  @Override
  public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
    registrar.setValidator(validator);
  }

  //  @Bean
  //  public ConcurrentKafkaListenerContainerFactory<String, Object>
  // deadLetterKafkaListenerContainerFactory() {}

  //  @Bean
  //  public ConcurrentKafkaListenerContainerFactory<String, Object>
  // autoCommitKafkaListenerContainerFactory() {}

}
