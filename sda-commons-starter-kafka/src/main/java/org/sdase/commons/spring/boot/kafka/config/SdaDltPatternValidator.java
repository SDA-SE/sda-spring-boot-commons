/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka.config;

import org.apache.commons.lang3.StringUtils;
import org.sdase.commons.spring.boot.kafka.SdaKafkaConsumerConfiguration;
import org.springframework.lang.NonNull;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class SdaDltPatternValidator implements Validator {
  @Override
  public boolean supports(@NonNull Class<?> clazz) {
    return KafkaConsumerConfig.class.isAssignableFrom(clazz);
  }

  @Override
  public void validate(@NonNull Object target, @NonNull Errors errors) {

    KafkaConsumerConfig kafkaConsumerConfig = (KafkaConsumerConfig) target;
    if (kafkaConsumerConfig.dlt() != null
        && StringUtils.isNotBlank(kafkaConsumerConfig.dlt().pattern())
        && (!kafkaConsumerConfig
            .dlt()
            .pattern()
            .contains(SdaKafkaConsumerConfiguration.DLT_REGEX))) {
      errors.rejectValue(
          "dlt.pattern",
          "invalid value: ",
          "sda.kafka.consumer.dlt.pattern property must to contain '"
              + SdaKafkaConsumerConfiguration.DLT_REGEX
              + "'");
    }
  }
}
