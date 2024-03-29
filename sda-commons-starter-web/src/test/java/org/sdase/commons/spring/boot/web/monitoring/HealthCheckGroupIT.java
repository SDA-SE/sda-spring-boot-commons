/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.auth.opa.OpenPolicyAgentHealthIndicator;
import org.sdase.commons.spring.boot.web.monitoring.testing.HealthyHealthIndicator;
import org.sdase.commons.spring.boot.web.monitoring.testing.MonitoringTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalManagementPort;

@SpringBootTest(
    classes = MonitoringTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "management.endpoint.health.group.readiness.include=readinessState, unhealthy, openPolicyAgent",
      "test.tracing.client.base.url=http://localhost:${wiremock.server.port}/feign",
      "management.server.port=8087" // We need to set a fixed port here, hence the random port to
      // exclude the management requests are not set probably at
      // runtime
    })
class HealthCheckGroupIT {

  @LocalManagementPort private int managementPort;

  @Autowired TestRestTemplate client;

  @Autowired HealthyHealthIndicator healthyHealthIndicator;

  @Autowired OpenPolicyAgentHealthIndicator openPolicyAgentHealthIndicator;

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
