/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import tools.jackson.databind.json.JsonMapper;

@AutoConfiguration
@PropertySource("classpath:/org/sdase/commons/spring/boot/kafka/producer.properties")
public class SdaKafkaProducerConfiguration {

  //  public to allow clients to configure their own templates
  public final Map<String, Object> commonProperties =
      Map.of(
          ProducerConfig.INTERCEPTOR_CLASSES_CONFIG,
          MetadataContextProducerInterceptor.class.getName()
              + ","
              + TraceTokenProducerInterceptor.class.getName());

  private final JsonMapper jsonMapper;

  public SdaKafkaProducerConfiguration(JsonMapper jsonMapper) {
    this.jsonMapper = jsonMapper;
  }

  @Bean
  @Primary
  @SuppressWarnings("java:S1452")
  public KafkaTemplate<String, ?> kafkaTemplate(ProducerFactory<String, ?> producerFactory) {

    Map<String, Object> props = new HashMap<>(commonProperties);
    //    use new factory to allow adding different serializers in DLT factory
    ProducerFactory<String, ?> producerFactoryCustom =
        producerFactory.copyWithConfigurationOverride(props);

    //    only set the JsonSerializer including the ObjectMapper if set as value.serializer in
    //    configuration otherwise allow different serializers to be set
    Object valueSerializerClass =
        producerFactory
            .getConfigurationProperties()
            .get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG);

    if (valueSerializerClass instanceof Class<?> clazz
        && (clazz.isAssignableFrom(JacksonJsonSerializer.class))) {
      ((DefaultKafkaProducerFactory<?, ?>) producerFactoryCustom)
          .setValueSerializer(new JacksonJsonSerializer<>(jsonMapper));
    }
    return new KafkaTemplate<>(producerFactoryCustom, props);
  }

  /* DLT Producer is using a value ByteArraySerializer
   * to be more flexible and allow teams
   * to choose the serializer for the original message
   * configured in this property 'spring.kafka.producer.value-serializer'
   *
   * The DLT consumer can use a deserializer that matches 'spring.kafka.producer.value-serializer'
   */
  @Bean("kafkaByteArrayDltTemplate")
  @SuppressWarnings("java:S1452")
  public KafkaTemplate<String, ?> kafkaByteArrayDltTemplate(
      ProducerFactory<String, ?> producerFactory) {

    Map<String, Object> props = new HashMap<>(commonProperties);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
    ProducerFactory<String, ?> producerFactoryByte =
        producerFactory.copyWithConfigurationOverride(props);

    return new KafkaTemplate<>(producerFactoryByte, props);
  }
}
