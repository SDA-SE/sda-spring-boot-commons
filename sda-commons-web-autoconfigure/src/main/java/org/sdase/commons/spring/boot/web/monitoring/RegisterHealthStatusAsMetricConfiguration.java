/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;

/**
 * Configuration making results from {@link HealthIndicator}s also available as metrics in
 * Monitoring like in the dropwizard services. This allows to create dashboards displaying the
 * health status of a service independent of which framework used to implement a service.
 */
@AutoConfiguration
public class RegisterHealthStatusAsMetricConfiguration {
  private static final String HEALTHCHECK_METRIC_NAME = "healthcheck_status";

  private final MeterRegistry registry;
  private final ApplicationContext context;

  public RegisterHealthStatusAsMetricConfiguration(
      MeterRegistry registry, ApplicationContext context) {
    this.registry = registry;
    this.context = context;
  }

  @PostConstruct
  void init() {
    Map<String, HealthIndicator> indicators = context.getBeansOfType(HealthIndicator.class);
    for (Map.Entry<String, HealthIndicator> entry : indicators.entrySet()) {
      String tagName = entry.getKey();
      registerHealthcheck(tagName, entry.getValue());
    }
  }

  private void registerHealthcheck(String tagName, HealthIndicator healthIndicator) {
    registry.gauge(
        HEALTHCHECK_METRIC_NAME,
        List.of(Tag.of("name", tagName)),
        healthIndicator,
        ind -> ind.health().getStatus() == Status.UP ? 1 : 0);
  }
}
