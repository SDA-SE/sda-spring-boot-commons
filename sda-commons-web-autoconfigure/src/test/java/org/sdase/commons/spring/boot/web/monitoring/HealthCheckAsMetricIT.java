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
import org.sdase.commons.spring.boot.web.monitoring.testing.MonitoringTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = MonitoringTestApp.class,
    properties = {
      "test.tracing.client.base.url=http://localhost:${wiremock.server.port}/feign",
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
}
