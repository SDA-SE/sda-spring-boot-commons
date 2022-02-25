/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.monitoring.testing.MonitoringTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.server.LocalManagementPort;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;

@SpringBootTest(
    classes = MonitoringTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "management.server.port=0",
      "test.tracing.client.base.url=http://localhost:${wiremock.server.port}/feign",
    })
@EnableAutoConfiguration(
    exclude = {SecurityAutoConfiguration.class, ManagementWebSecurityAutoConfiguration.class})
@AutoConfigureMetrics
class MetricsIT {

  @LocalManagementPort private int managementPort;

  @Autowired TestRestTemplate client;

  @Test
  @SuppressWarnings("unchecked")
  void shouldReturnGeneralMetrics() {
    var responseEntity = requestMonitoring("", Map.class);
    responseEntity = requestMonitoring("metrics", Map.class);
    assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
    assertThat(responseEntity.getBody()).extractingByKeys("names").isNotEmpty();
  }

  @Test
  @Disabled("For some reasons there is a bug with prometheus. in full integration test its working")
  void shouldReturnPrometheusMetrics() {
    var responseEntity = requestMonitoring("metrics/prometheus", Object.class);
    assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
    assertThat(responseEntity.getBody()).isNotNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldReturnHavePrometheusMetricsEndpoint() {
    var responseEntity = requestMonitoring("", Map.class);
    assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
    Map<String, Object> result = responseEntity.getBody();
    assertThat(result).extractingByKeys("_links").isNotEmpty();
    assertThat(((Map<String, Object>) result.get("_links")).get("prometheus")).isNotNull();
  }

  private <T> ResponseEntity<T> requestMonitoring(String subpath, Class<T> clazz) {
    return client.getForEntity(
        String.format("http://localhost:%d/%s", managementPort, subpath), clazz);
  }
}
