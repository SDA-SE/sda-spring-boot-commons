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

import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.monitoring.testing.CustomMetricsTestController;
import org.sdase.commons.spring.boot.web.monitoring.testing.MonitoringTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
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
      "management.tracing.enabled=true"
    })
@AutoConfigureObservability
@AutoConfigureMockMvc
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
@DirtiesContext(classMode = ClassMode.BEFORE_EACH_TEST_METHOD)
class MetricsIT {

  @LocalManagementPort private int managementPort;

  @Autowired TestRestTemplate client;

  @Autowired CustomMetricsTestController customMetricsTestController;

  @Autowired private MockMvc mockMvc;

  @Autowired MeterRegistry meterRegistry;

  @BeforeEach
  void beforeEach() throws Exception {
    populateDefaultAndCustomMetrics();
    ensureGcMetrics();
  }

  @AfterEach
  void afterEach() {
    meterRegistry.clear();
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldReturnGeneralMetrics() {
    var responseEntity = getForEntity("metrics", Map.class);
    assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
    assertThat(responseEntity.getBody())
        .flatExtracting("names")
        .isNotEmpty()
        .containsAll(METRIC_NAMES);
  }

  @Test
  @SuppressWarnings("unchecked")
  void shouldReturnHavePrometheusMetricsEndpoint() {
    var responseEntity = getForEntity("", Map.class);
    assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
    Map<String, Object> result = responseEntity.getBody();
    assertThat(result).extractingByKeys("_links").isNotEmpty();
    assertThat(((Map<String, Object>) result.get("_links")).get("prometheus")).isNotNull();
  }

  @Test
  void shouldReturnPrometheusGeneralMetrics() {
    var responseEntity = getForEntity("metrics/prometheus", String.class);
    assertThat(responseEntity.getStatusCode().value()).isEqualTo(200);
    assertThat(responseEntity.getBody()).isNotNull().contains(PROMETHEUS_METRICS_NAMES);
  }

  @Test
  void shouldProduceSuccessCounterMetric() {
    String metrics = getForEntity("metrics/prometheus", String.class).getBody();

    assertThat(metrics)
        .contains("Counts successes occurred when some operation is invoked.")
        .contains("# TYPE some_operation_success_counter_total counter");
  }

  @Test
  void shouldProduceHttpServerMetricWithPathVariable() {
    String metrics = getForEntity("metrics/prometheus", String.class).getBody();

    assertThat(metrics)
        .contains(
            "http_server_requests_seconds_count{error=\"none\",exception=\"none\",method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"/add-custom-metrics/{name}\",} ")
        .contains(
            "http_server_requests_seconds_sum{error=\"none\",exception=\"none\",method=\"GET\",outcome=\"SUCCESS\",status=\"200\",uri=\"/add-custom-metrics/{name}\",} ");
  }

  @Test
  void shouldProduceErrorCounterMetric() {
    String metrics = getForEntity("metrics/prometheus", String.class).getBody();

    assertThat(metrics)
        .contains("Counts errors occurred when some operation is invoked.")
        .contains("# TYPE some_operation_error_counter_total counter");
  }

  private <T> ResponseEntity<T> getForEntity(String subpath, Class<T> clazz) {
    return client.getForEntity(
        String.format("http://localhost:%d/%s", managementPort, subpath), clazz);
  }

  private void populateDefaultAndCustomMetrics() throws Exception {
    // just do some operations in my service to generate test data
    for (int i = 0; i < 10; i++) {
      mockMvc
          .perform(
              MockMvcRequestBuilders.request(HttpMethod.GET, "/add-custom-metrics")
                  .accept(APPLICATION_JSON))
          .andExpect(MockMvcResultMatchers.status().isOk());
      mockMvc
          .perform(
              MockMvcRequestBuilders.request(HttpMethod.GET, "/add-custom-metrics/John")
                  .accept(APPLICATION_JSON))
          .andExpect(MockMvcResultMatchers.status().isOk());
      mockMvc
          .perform(
              MockMvcRequestBuilders.request(HttpMethod.GET, "/add-custom-metrics/Jane")
                  .accept(APPLICATION_JSON))
          .andExpect(MockMvcResultMatchers.status().isOk());
    }
    // calling a route to populate http requests metrics
    getForEntity("", Map.class);
  }

  /** for ensuring gc metrics generation */
  private static void ensureGcMetrics() {
    System.gc();
  }
}
