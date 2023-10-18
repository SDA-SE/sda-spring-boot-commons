/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@AutoConfiguration
@PropertySource("classpath:/org/sdase/commons/spring/boot/kafka/producer.properties")
public class SdaKafkaProducerConfiguration {

  private final Map<String, Object> commonProperties =
      Map.of(
          ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
          MetadataContextProducerInterceptor.class.getName());

  private final ObjectMapper objectMapper;

  public SdaKafkaProducerConfiguration(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Bean
  @SuppressWarnings("java:S1452")
  public KafkaTemplate<String, ?> kafkaTemplate(ProducerFactory<String, ?> producerFactory) {
    ((DefaultKafkaProducerFactory<?, ?>) producerFactory)
        .setValueSerializer(new JsonSerializer<>(objectMapper));
    return new KafkaTemplate<>(producerFactory, commonProperties);
  }

  /* DLT Producer is using a value ByteArraySerializer
   * to be more flexible and allow teams
   * to choose the serializer for the original message
   * configured in this property 'spring.kafka.producer.value-serializer'
   *
   * The DLT consumer can use a deserializer that matches 'spring.kafka.producer.value-serializer'
   */
  @Bean
  @SuppressWarnings("java:S1452")
  public KafkaTemplate<Object, Object> kafkaDltTemplate(
      ProducerFactory<Object, Object> producerFactory) {
    Map<String, Object> props = new HashMap<>(commonProperties);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
    return new KafkaTemplate<>(producerFactory, props);
  }
}
