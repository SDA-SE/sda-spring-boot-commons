/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.kafka.config;

import javax.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

@ConstructorBinding
@ConfigurationProperties(prefix = "sda.kafka.consumer")
public record KafkaConsumerConfig(@NotNull RetryConfig retry) {
  public record RetryConfig(
      @NotNull Integer maxRetries,
      @NotNull Long initialBackOffInterval,
      @NotNull Long maxBackOffInterval,
      @NotNull Long backOffMultiplier) {}
}
