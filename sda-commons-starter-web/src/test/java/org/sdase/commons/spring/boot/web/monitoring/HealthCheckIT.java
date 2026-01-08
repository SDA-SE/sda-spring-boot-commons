/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.sdase.commons.spring.boot.web.auth.opa.model.OpaResponse;
import org.sdase.commons.spring.boot.web.monitoring.testing.MonitoringTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;

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
@AutoConfigureTestRestTemplate
@SuppressWarnings("unchecked")
class HealthCheckIT {

  @LocalManagementPort private int managementPort;

  @Autowired TestRestTemplate client;

  @MockitoBean
  @Qualifier("opaRestTemplate")
  private RestTemplate opaRestTemplate;

  @AfterEach
  void afterEach() {
    Mockito.reset(opaRestTemplate);
  }

  @ParameterizedTest
  @ValueSource(strings = {"readiness", "liveness", "healthy", "ping"})
  void shouldBeHealthy(String healthEndpoint) {
    when(opaRestTemplate.getForObject(anyString(), eq(OpaResponse.class)))
        .thenReturn(getAllowedResponse(false));
    var responseEntity = getHealthCheck(healthEndpoint);
    assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(responseEntity.getBody()).extractingByKeys("status").containsExactly("UP");
  }

  @Test
  void shouldReturnOpaHealthCheckConnectionRefused() {
    when(opaRestTemplate.getForObject(anyString(), eq(OpaResponse.class)))
        .thenThrow(new HttpClientErrorException(HttpStatus.BAD_GATEWAY, "Bad gateway"));
    var responseEntity = getHealthCheck("openPolicyAgent");
    assertThat(responseEntity.getStatusCode().is5xxServerError()).isTrue();
    assertThat(responseEntity.getBody()).extractingByKey("status").isEqualTo("DOWN");
    assertThat(responseEntity.getBody().get("details")).isNotNull();
    assertThat((LinkedHashMap) responseEntity.getBody().get("details"))
        .extractingByKey("healthy")
        .isEqualTo(false);
    assertThat((LinkedHashMap) responseEntity.getBody().get("details"))
        .extractingByKey("message")
        .isEqualTo("502 Bad gateway");
  }

  @ParameterizedTest(name = "[{index}] OpaResponse")
  @MethodSource("getInvalidOpaResponses")
  void shouldReturnOpaHealthCheckInvalidResponse(OpaResponse opaResponse) {
    when(opaRestTemplate.getForObject(anyString(), eq(OpaResponse.class))).thenReturn(opaResponse);
    var responseEntity = getHealthCheck("openPolicyAgent");
    assertThat(responseEntity.getStatusCode().is5xxServerError()).isTrue();
    assertThat(responseEntity.getBody()).extractingByKeys("status").containsExactly("DOWN");
    assertThat(responseEntity.getBody().get("details")).isNotNull();
    assertThat((LinkedHashMap) responseEntity.getBody().get("details"))
        .extractingByKey("healthy")
        .isEqualTo(false);
    assertThat((LinkedHashMap) responseEntity.getBody().get("details"))
        .extractingByKey("message")
        .isEqualTo("The policy response seems not to be SDA guideline compliant");
  }

  @Test
  void shouldReturnOpaHealthCheckDenyDecisionByDefault() {
    when(opaRestTemplate.getForObject(anyString(), eq(OpaResponse.class)))
        .thenReturn(getAllowedResponse(true));
    var responseEntity = getHealthCheck("openPolicyAgent");
    assertThat(responseEntity.getStatusCode().is5xxServerError()).isTrue();
    assertThat(responseEntity.getBody()).extractingByKeys("status").containsExactly("DOWN");
    assertThat(responseEntity.getBody().get("details")).isNotNull();
    assertThat((LinkedHashMap) responseEntity.getBody().get("details"))
        .extractingByKey("healthy")
        .isEqualTo(false);
    assertThat((LinkedHashMap) responseEntity.getBody().get("details"))
        .extractingByKey("message")
        .isEqualTo("The policy should respond with a deny decision by default");
  }

  @Test
  void shouldReturnOpaHealthSuccess() {
    when(opaRestTemplate.getForObject(anyString(), eq(OpaResponse.class)))
        .thenReturn(getAllowedResponse(false));
    var responseEntity = getHealthCheck("openPolicyAgent");
    assertThat(responseEntity.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(responseEntity.getBody()).extractingByKeys("status").containsExactly("UP");
    assertThat(responseEntity.getBody().get("details")).isNotNull();
    assertThat((LinkedHashMap) responseEntity.getBody().get("details"))
        .extractingByKey("healthy")
        .isEqualTo(true);
    assertThat((LinkedHashMap) responseEntity.getBody().get("details"))
        .extractingByKey("message")
        .isNull();
  }

  private static Stream<Arguments> getInvalidOpaResponses() {
    return Stream.of(
        null,
        Arguments.of(new OpaResponse().setResult(null)),
        Arguments.of(new OpaResponse().setResult(NullNode.getInstance())));
  }

  private OpaResponse getAllowedResponse(boolean allow) {
    ObjectNode objectNode = new ObjectMapper().createObjectNode().put("allow", allow);
    return new OpaResponse().setResult(objectNode);
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
