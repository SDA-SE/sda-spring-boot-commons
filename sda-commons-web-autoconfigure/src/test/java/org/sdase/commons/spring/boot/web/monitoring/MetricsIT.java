/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sdase.commons.spring.boot.web.monitoring.testing.MetricsConstants.METRIC_NAMES;
import static org.sdase.commons.spring.boot.web.monitoring.testing.MetricsConstants.PROMETHEUS_METRICS_NAMES;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.sdase.commons.spring.boot.web.monitoring.testing.CustomMetricsTestController;
import org.sdase.commons.spring.boot.web.monitoring.testing.MonitoringTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(
    classes = MonitoringTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "management.server.port=8083",
      // We need to set a fixed port here, hence the random port to
      // exclude the management requests are not set probably at
      // runtime
      "test.tracing.client.base.url=http://localhost:${wiremock.server.port}/feign",
      "opa.disable=true",
    })
@AutoConfigureMetrics
@AutoConfigureMockMvc
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
@TestMethodOrder(value = MethodOrderer.OrderAnnotation.class)
class MetricsIT {

  private static final Log LOG = LogFactory.getLog(MetricsIT.class);

  @LocalManagementPort private int managementPort;

  @Autowired TestRestTemplate client;

  @Autowired CustomMetricsTestController customMetricsTestController;

  @Autowired private MockMvc mockMvc;

  @Test
  @SuppressWarnings("unchecked")
  @Order(0)
  void shouldReturnGeneralMetrics() {
    var responseEntity = requestMonitoring("metrics", Map.class);
    assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
    LOG.info(String.join(",", (ArrayList<String>) responseEntity.getBody().get("names")));
    assertThat(responseEntity.getBody())
        .extractingByKeys("names")
        .isNotEmpty()
        .containsExactlyInAnyOrder(METRIC_NAMES);
  }

  @Test
  @SuppressWarnings("unchecked")
  @Order(1)
  void shouldReturnHavePrometheusMetricsEndpoint() {
    var responseEntity = requestMonitoring("", Map.class);
    assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
    Map<String, Object> result = responseEntity.getBody();
    assertThat(result).extractingByKeys("_links").isNotEmpty();
    assertThat(((Map<String, Object>) result.get("_links")).get("prometheus")).isNotNull();
  }

  @Test
  @Order(2)
  void shouldReturnPrometheusGeneralMetrics() {
    var responseEntity = requestMonitoring("metrics/prometheus", String.class);
    LOG.info(String.join(",", responseEntity.getBody().split("\\n")));
    assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
    assertThat(responseEntity.getBody()).isNotNull().contains(PROMETHEUS_METRICS_NAMES);
  }

  @Test
  @Order(3)
  void shouldProduceSuccessCounterMetric() throws Exception {
    addMetrics();
    String metrics = requestMonitoring("metrics/prometheus", String.class).getBody();

    assertThat(metrics)
        .contains("Counts successes occurred when some operation is invoked.")
        .contains("# TYPE some_operation_success_counter_total counter");
  }

  @Test
  @Order(4)
  void shouldProduceErrorCounterMetric() throws Exception {
    addMetrics();
    String metrics = requestMonitoring("metrics/prometheus", String.class).getBody();

    assertThat(metrics)
        .contains("Counts errors occurred when some operation is invoked.")
        .contains("# TYPE some_operation_error_counter_total counter");
  }

  private <T> ResponseEntity<T> requestMonitoring(String subpath, Class<T> clazz) {
    return client.getForEntity(
        String.format("http://localhost:%d/%s", managementPort, subpath), clazz);
  }

  private void addMetrics() throws Exception {
    // just do some operations in my service to generate test data
    for (int i = 0; i < 10; i++) {
      mockMvc
          .perform(
              MockMvcRequestBuilders.request(HttpMethod.GET, "/add-custom-metrics")
                  .accept(APPLICATION_JSON))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }
  }
}
