/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka.events;

import jakarta.validation.constraints.NotNull;
import org.springframework.context.ApplicationEvent;

/**
 * Event that is published when the health of the Kafka cluster changes.
 * Can be used in combination with <a href="https://docs.spring.io/spring-kafka/reference/kafka/events.html">Application Events</a>
 * to react to health changes.
 */
public class SdaKafkaHealthEvent extends ApplicationEvent {

  boolean isHealthy;
  private String message;

  public SdaKafkaHealthEvent(Object source, boolean isHealthy, @NotNull String message) {
    super(source);
    this.isHealthy = isHealthy;
    this.message = message;
  }

  public boolean isHealthy() {
    return isHealthy;
  }

  public String getMessage() {
    return message;
  }
}
