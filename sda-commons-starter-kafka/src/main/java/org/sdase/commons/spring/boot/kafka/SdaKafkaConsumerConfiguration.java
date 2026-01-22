/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.sdase.commons.spring.boot.kafka.config.KafkaConsumerConfig;
import org.sdase.commons.spring.boot.kafka.config.SdaDltPatternValidator;
import org.sdase.commons.spring.boot.kafka.config.SdaKafkaListenerContainerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.annotation.KafkaListenerConfigurer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistrar;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.CommonContainerStoppingErrorHandler;
import org.springframework.kafka.listener.CommonErrorHandler;
import org.springframework.kafka.listener.CommonLoggingErrorHandler;
import org.springframework.kafka.listener.CompositeRecordInterceptor;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.kafka.support.converter.ByteArrayJsonMessageConverter;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

@AutoConfiguration
@PropertySource("classpath:/org/sdase/commons/spring/boot/kafka/consumer.properties")
@ConfigurationPropertiesScan
public class SdaKafkaConsumerConfiguration implements KafkaListenerConfigurer {

  private static final Logger LOG = LoggerFactory.getLogger(SdaKafkaConsumerConfiguration.class);

  /**
   * Only used if {@code sda.kafka.consumer.dlt.pattern} is actively set to empty as a fallback to
   * the spring-boot default pattern.
   */
  public static final String DLT_SUFFIX = ".DLT";

  public static final String DLT_REGEX = "<topic>";
  private final KafkaProperties kafkaProperties;
  private final KafkaTemplate<String, ?> recoverTemplate;
  private final LocalValidatorFactoryBean validator;
  private final ObjectMapper objectMapper;
  private final KafkaConsumerConfig consumerConfig;

  public SdaKafkaConsumerConfiguration(
      KafkaProperties kafkaProperties,
      @Qualifier("kafkaByteArrayDltTemplate") KafkaTemplate<String, ?> recoverTemplate,
      LocalValidatorFactoryBean validator,
      ObjectMapper objectMapper,
      KafkaConsumerConfig consumerConfig) {
    this.kafkaProperties = kafkaProperties;
    this.recoverTemplate = recoverTemplate;
    this.validator = validator;
    this.objectMapper = objectMapper;
    this.consumerConfig = consumerConfig;
  }

  @Bean
  public static SdaDltPatternValidator configurationPropertiesValidator() {
    return new SdaDltPatternValidator();
  }

  @Bean(SdaKafkaListenerContainerFactory.RETRY_AND_LOG)
  @SuppressWarnings("java:S1452")
  public ConcurrentKafkaListenerContainerFactory<String, ?>
      retryAndLogKafkaListenerContainerFactory(
          @Qualifier("retryErrorHandler") CommonErrorHandler errorHandler) {
    return createDefaultListenerContainerFactory(errorHandler);
  }

  @Bean(SdaKafkaListenerContainerFactory.RETRY_AND_DLT)
  @SuppressWarnings("java:S1452")
  public ConcurrentKafkaListenerContainerFactory<String, ?>
      retryAndDltKafkaListenerContainerFactory(
          @Qualifier("retryDeadLetterErrorHandler") CommonErrorHandler errorHandler) {
    return createDefaultListenerContainerFactory(errorHandler);
  }

  @Bean(SdaKafkaListenerContainerFactory.LOG_ON_FAILURE)
  @SuppressWarnings("java:S1452")
  public ConcurrentKafkaListenerContainerFactory<String, ?>
      logOnFailureKafkaListenerContainerFactory(
          @Qualifier("loggingErrorHandler") CommonErrorHandler errorHandler) {
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

  @Bean("loggingErrorHandler")
  public CommonErrorHandler loggingErrorHandler() {
    return new CommonLoggingErrorHandler();
  }

  @Bean("containerStoppingErrorHandler")
  public CommonErrorHandler containerStoppingErrorHandler() {
    return new CommonContainerStoppingErrorHandler();
  }

  /**
   * By default, the dead-letter record is sent to a topic named &lt;originalTopic&gt;.DLT (the
   * original topic name suffixed with .DLT) and to the same partition as the original record.
   * Therefore, when you use the default resolver, the dead-letter topic must have at least as many
   * partitions as the original topic.
   *
   * @return the default error handler that retries according to the {@link
   *     KafkaConsumerConfig#retry()} before producing an error message
   */
  @Bean("retryDeadLetterErrorHandler")
  public DefaultErrorHandler retryDeadLetterErrorHandler(
      Map<Class<?>, KafkaOperations<?, ?>> dltTemplates) {
    // Will result into 1s, 2s, 4s, 4s retry backoff
    ExponentialBackOffWithMaxRetries backOff = createDefaultRetryBackOff();
    DeadLetterPublishingRecoverer recoverer =
        new DeadLetterPublishingRecoverer(dltTemplates, this::getDeadLetterTopicName);
    recoverer.setLogRecoveryRecord(true);
    DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, backOff);
    handler.addNotRetryableExceptions(NotRetryableKafkaException.class);
    return handler;
  }

  /**
   * DLT templates used serialize recods after exception is thrown in KafkaListener. Recover
   * template uses ByteArraySerializer to allow serialization of any type of record.
   *
   * <p>Can be overridden to provide custom templates, to provide different serializer from records
   * that were deserialized successfully
   *
   * <p>https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html#dead-letters
   */
  @Bean
  @Lazy
  @SuppressWarnings("java:S1452")
  public Map<Class<?>, KafkaOperations<?, ?>> dltTemplates() {
    Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
    templates.put(byte[].class, recoverTemplate);
    return templates;
  }

  protected TopicPartition getDeadLetterTopicName(
      ConsumerRecord<?, ?> consumerRecord, Exception exception) {

    if (consumerConfig.dlt() != null && StringUtils.isNotBlank(consumerConfig.dlt().pattern())) {

      try {

        String pattern = consumerConfig.dlt().pattern();
        String dltTopicName = pattern.replace(DLT_REGEX, consumerRecord.topic());
        return new TopicPartition(dltTopicName, consumerRecord.partition());
      } catch (Exception e) {

        LOG.error(
            "Custom DLT pattern " + consumerConfig.dlt().pattern() + ", could not be used", e);
      }
    }

    return new TopicPartition(consumerRecord.topic() + DLT_SUFFIX, consumerRecord.partition());
  }

  private ExponentialBackOffWithMaxRetries createDefaultRetryBackOff() {
    ExponentialBackOffWithMaxRetries backOff =
        new ExponentialBackOffWithMaxRetries(consumerConfig.retry().maxRetries());
    backOff.setInitialInterval(consumerConfig.retry().initialBackoffInterval());
    backOff.setMultiplier(consumerConfig.retry().backoffMultiplier());
    backOff.setMaxInterval(consumerConfig.retry().maxBackoffInterval());
    return backOff;
  }

  private ConcurrentKafkaListenerContainerFactory<String, ?> createDefaultListenerContainerFactory(
      CommonErrorHandler errorHandler) {
    ConcurrentKafkaListenerContainerFactory<String, ?> factory =
        new ConcurrentKafkaListenerContainerFactory<>();
    factory.setConsumerFactory(
        new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties(null)));
    factory.setRecordMessageConverter(new ByteArrayJsonMessageConverter(objectMapper));
    factory.getContainerProperties().setAckMode(AckMode.RECORD);
    // Please note that ConversionExceptions like mapping exception won't be retried and directly
    // logged to error. We may add some specific handling like DeadLetter topics etc.
    factory.setCommonErrorHandler(errorHandler);
    factory.setRecordInterceptor(
        new CompositeRecordInterceptor<>(
            new MetadataContextRecordInterceptor<>(), new TraceTokenRecordInterceptor<>()));
    return factory;
  }
}
