/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.sdase.commons.spring.boot.kafka.config.KafkaConsumerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@Configuration
@PropertySource("classpath:/org/sdase/commons/spring/boot/kafka/consumer.properties")
@ConfigurationPropertiesScan
public class SdaKafkaConsumerConfiguration implements KafkaListenerConfigurer {

  private final KafkaProperties kafkaProperties;
  private final KafkaTemplate<Object, Object> retryTemplate;
  private final LocalValidatorFactoryBean validator;
  private final ObjectMapper objectMapper;
  private final KafkaConsumerConfig consumerConfig;

  public SdaKafkaConsumerConfiguration(
      KafkaProperties kafkaProperties,
      KafkaTemplate<Object, Object> retryTemplate,
      LocalValidatorFactoryBean validator,
      ObjectMapper objectMapper,
      KafkaConsumerConfig consumerConfig) {
    this.kafkaProperties = kafkaProperties;
    this.retryTemplate = retryTemplate;
    this.validator = validator;
    this.objectMapper = objectMapper;
    this.consumerConfig = consumerConfig;
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object>
      retryAndLogKafkaListenerContainerFactory(
          @Qualifier("retryErrorHandler") CommonErrorHandler errorHandler) {
    return createDefaultListenerContainerFactory(errorHandler);
  }

  @Bean
  public ConcurrentKafkaListenerContainerFactory<String, Object>
      retryAndDldKafkaListenerContainerFactory(
          @Qualifier("retryDeadLetterErrorHandler") CommonErrorHandler errorHandler) {
    return createDefaultListenerContainerFactory(errorHandler);
  }

  @Override
  public void configureKafkaListeners(KafkaListenerEndpointRegistrar registrar) {
    registrar.setValidator(validator);
  }

  @Bean("retryErrorHandler")
  public DefaultErrorHandler retryErrorHandler() {
    // Will result into 1s, 2s, 4s, 4s retry backoff
    ExponentialBackOffWithMaxRetries backOff = createDefaultRetryBackOff();
    DefaultErrorHandler handler = new DefaultErrorHandler(backOff);
    handler.addNotRetryableExceptions(NotRetryableKafkaException.class);
    return handler;
  }

  /**
   * By default, the dead-letter record is sent to a topic named <originalTopic>.DLT (the original
   * topic name suffixed with .DLT) and to the same partition as the original record. Therefore,
   * when you use the default resolver, the dead-letter topic must have at least as many partitions
   * as the original topic.
   */
  @Bean("retryDeadLetterErrorHandler")
  public DefaultErrorHandler retryDeadLetterErrorHandler() {
    // Will result into 1s, 2s, 4s, 4s retry backoff
    ExponentialBackOffWithMaxRetries backOff = createDefaultRetryBackOff();
    DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(retryTemplate);
    DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
    handler.addNotRetryableExceptions(NotRetryableKafkaException.class);
    return handler;
  }

  private ExponentialBackOffWithMaxRetries createDefaultRetryBackOff() {
    ExponentialBackOffWithMaxRetries backOff =
        new ExponentialBackOffWithMaxRetries(consumerConfig.retry().maxRetries());
    backOff.setInitialInterval(consumerConfig.retry().initialBackOffInterval());
    backOff.setMultiplier(consumerConfig.retry().backOffMultiplier());
    backOff.setMaxInterval(consumerConfig.retry().maxBackOffInterval());
    return backOff;
  }

  private ConcurrentKafkaListenerContainerFactory<String, Object>
      createDefaultListenerContainerFactory(CommonErrorHandler errorHandler) {
    ConcurrentKafkaListenerContainerFactory<String, Object> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(
        new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties()));
    factory.setMessageConverter(new ByteArrayJsonMessageConverter(objectMapper));
    factory.getContainerProperties().setAckMode(AckMode.RECORD);
    // Please note that ConversionExceptions like mapping exception won't be retried and directly
    // logged to error. We may add some specific handling like DeadLetter topics etc.
    factory.setCommonErrorHandler(errorHandler);
    return factory;
  }
}
