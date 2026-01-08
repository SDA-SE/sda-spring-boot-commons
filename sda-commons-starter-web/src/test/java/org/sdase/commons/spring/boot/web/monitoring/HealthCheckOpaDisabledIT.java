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

import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@SpringBootTest(
    classes = MonitoringTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "management.server.port=8085",
      "opa.disable=true",
      // We need to set a fixed port here, hence the random port to
      // exclude the management requests are not set probably at
      // runtime
      "test.tracing.client.base.url=http://localhost:${wiremock.server.port}/feign",
    })
@ExtendWith(SpringExtension.class)
@AutoConfigureTestRestTemplate
class HealthCheckOpaDisabledIT {

  @LocalManagementPort private int managementPort;

  @Autowired TestRestTemplate client;

  @MockitoBean
  @Qualifier("opaRestTemplate")
  private RestTemplate opaRestTemplate;

  @AfterEach
  void afterEach() {
    Mockito.reset(opaRestTemplate);
  }

  @Test
  void shouldReturnOpaHealthNotFoundForOpaDisabled() {
    when(opaRestTemplate.getForObject(anyString(), eq(OpaResponse.class)))
        .thenReturn(getAllowedResponse());
    var responseEntity = getHealthCheck();
    assertThat(responseEntity.getStatusCode().is4xxClientError()).isTrue();
    assertThat(responseEntity.getStatusCode().value()).isEqualTo(404);
  }

  private OpaResponse getAllowedResponse() {
    ObjectNode objectNode = new ObjectMapper().createObjectNode().put("allow", false);
    return new OpaResponse().setResult(objectNode);
  }

  private ResponseEntity<Map> getHealthCheck() {
    return client.getForEntity(
        String.format("http://localhost:%d/healthcheck/%s", managementPort, "openPolicyAgent"),
        Map.class);
  }
}
