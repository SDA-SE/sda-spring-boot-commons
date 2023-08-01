/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.monitoring.testing.HealthyHealthIndicator;
import org.sdase.commons.spring.boot.web.monitoring.testing.UnhealthyHealthIndicator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@SpringBootTest(
    classes = {
      RegisterHealthStatusAsMetricConfiguration.class,
      HealthCheckAsMetricIT.AddSimpleMeterRegistryConfig.class,
      HealthyHealthIndicator.class,
      UnhealthyHealthIndicator.class
    })
class HealthCheckAsMetricIT {

  @Autowired private SimpleMeterRegistry registry;

  @Test
  void shouldAddHealthcheckToMetrics() {
    Collection<String> allMeters = Arrays.asList(registry.getMetersAsString().split("\n"));

    assertThat(allMeters)
        .anyMatch(str -> str.matches("healthcheck_status.*healthyHealthIndicator.*1.0"))
        .anyMatch(str -> str.matches("healthcheck_status.*unhealthyHealthIndicator.*0.0"));
  }

  @Configuration
  public static class AddSimpleMeterRegistryConfig {

    @Bean
    @Primary
    public SimpleMeterRegistry simpleMeterRegistry() {
      return new SimpleMeterRegistry();
    }
  }
}
