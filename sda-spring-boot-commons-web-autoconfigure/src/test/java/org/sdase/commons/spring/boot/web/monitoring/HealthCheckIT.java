/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.sdase.commons.spring.boot.web.monitoring.testing.MonitoringTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.web.server.LocalManagementPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    classes = MonitoringTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "management.server.port=8082",
      // We need to set a fixed port here, hence the random port to
      // exclude the management requests are not set probably at
      // runtime
      "test.tracing.client.base.url=http://localhost:${wiremock.server.port}/feign",
    })
class HealthCheckIT {

  @LocalManagementPort private int managementPort;

  @Autowired TestRestTemplate client;

  @SuppressWarnings("unchecked")
  @ParameterizedTest
  @ValueSource(strings = {"readiness", "liveness", "healthy", "ping"})
  void shouldBeHealthy(String healthEndpoint) {
    var responseEntity = getHealthCheck(healthEndpoint);
    assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(responseEntity.getBody()).extractingByKeys("status").containsExactly("UP");
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldReturnCustomHealthCheckDown() {
    var responseEntity = getHealthCheck("unhealthy");
    assertThat(responseEntity.getStatusCode().is5xxServerError()).isTrue();
    assertThat(responseEntity.getBody()).extractingByKeys("status").containsExactly("DOWN");
  }

  private ResponseEntity<Map> getHealthCheck(String healthCheck) {
    return client.getForEntity(
        String.format("http://localhost:%d/healthcheck/%s", managementPort, healthCheck),
        Map.class);
  }
}
