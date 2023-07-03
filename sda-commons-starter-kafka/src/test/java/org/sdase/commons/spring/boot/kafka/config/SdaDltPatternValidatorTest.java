/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;

class SdaDltPatternValidatorTest {

  private SdaDltPatternValidator validator;

  private KafkaConsumerConfig.RetryConfig retryConfig;

  @BeforeEach
  void setUp() {
    validator = new SdaDltPatternValidator();
    retryConfig = new KafkaConsumerConfig.RetryConfig(1, 1L, 1L, 1L);
  }

  @Test
  void validateDlTNull() {

    KafkaConsumerConfig kafkaConsumerConfig = new KafkaConsumerConfig(retryConfig, null);

    Errors errors = new BeanPropertyBindingResult(kafkaConsumerConfig, "kafkaConsumerConfig");
    validator.validate(kafkaConsumerConfig, errors);

    assertFalse(errors.hasErrors());
  }

  @Test
  void validatePatternNull() {

    KafkaConsumerConfig.DLTConfig dltConfig = new KafkaConsumerConfig.DLTConfig(null);
    KafkaConsumerConfig kafkaConsumerConfig = new KafkaConsumerConfig(retryConfig, dltConfig);

    Errors errors = new BeanPropertyBindingResult(kafkaConsumerConfig, "kafkaConsumerConfig");
    validator.validate(kafkaConsumerConfig, errors);

    assertFalse(errors.hasErrors());
  }

  @Test
  void validateNoTopic() {

    KafkaConsumerConfig.DLTConfig dltConfig =
        new KafkaConsumerConfig.DLTConfig("prefix-<no-topic>");
    KafkaConsumerConfig kafkaConsumerConfig = new KafkaConsumerConfig(retryConfig, dltConfig);

    Errors errors = new BeanPropertyBindingResult(kafkaConsumerConfig, "kafkaConsumerConfig");
    validator.validate(kafkaConsumerConfig, errors);

    assertTrue(errors.hasErrors());
    assertNotNull(errors.getFieldError("dlt.pattern"));
  }

  @Test
  void validateContainsValidTopic() {

    KafkaConsumerConfig.DLTConfig dltConfig = new KafkaConsumerConfig.DLTConfig("prefix-<topic>");
    KafkaConsumerConfig kafkaConsumerConfig = new KafkaConsumerConfig(retryConfig, dltConfig);

    Errors errors = new BeanPropertyBindingResult(kafkaConsumerConfig, "kafkaConsumerConfig");
    validator.validate(kafkaConsumerConfig, errors);

    assertFalse(errors.hasErrors());
  }
}
