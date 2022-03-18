/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.starter.monitoring.testing.HealthyHealthIndicator;
import org.sdase.commons.spring.boot.starter.monitoring.testing.MonitoringTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.web.server.LocalManagementPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;

@SpringBootTest(
    classes = MonitoringTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "management.endpoint.health.group.readiness.include=readinessState, unhealthy",
      "test.tracing.client.base.url=http://localhost:${wiremock.server.port}/feign",
      "management.server.port=0"
    })
class HealthCheckGroupIT {

  @LocalManagementPort private int managementPort;

  @Autowired TestRestTemplate client;

  @Autowired HealthyHealthIndicator healthyHealthIndicator;

  @Test
  @SuppressWarnings("unchecked")
  void shouldReturnReadinessDown() {
    var responseEntity =
        client.getForEntity(
            String.format("http://localhost:%d/healthcheck/readiness", managementPort), Map.class);
    assertThat(responseEntity.getStatusCode().is5xxServerError()).isTrue();
    assertThat(responseEntity.getBody()).extractingByKeys("status").containsExactly("DOWN");
  }
}
