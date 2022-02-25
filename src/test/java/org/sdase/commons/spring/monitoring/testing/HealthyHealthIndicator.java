/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.monitoring.testing;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
public class HealthyHealthIndicator implements HealthIndicator {

  @Override
  public Health health() {
    return new Health.Builder().up().build();
  }
}
