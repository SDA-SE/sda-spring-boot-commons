/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth.management;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.mcp.server.TestApplication;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.annotation.WebEndpointDiscoverer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = TestApplication.class,
    webEnvironment = RANDOM_PORT,
    properties = {
      "management.server.port=0",
      "management.endpoints.web.exposure.include=*",
      "management.endpoint.health.show-details=always",
      "management.prometheus.metrics.export.enabled=true"
    })
@ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
class ManagementPortActuatorAccessIT {

  @LocalServerPort private int applicationPort;

  @LocalManagementPort private int managementPort;

  @Autowired private TestRestTemplate restTemplate;

  @Autowired private AuthMock authMock;

  @Autowired private WebEndpointDiscoverer webEndpointDiscoverer;

  @Test
  void shouldAllowUnauthenticatedAccessToHealthEndpointOnManagementPort() {
    authMock.authorizeAnyRequest().deny();

    ResponseEntity<String> response =
        restTemplate.getForEntity(
            String.format("http://localhost:%d/actuator/health", managementPort), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("UP");
  }

  @Test
  void shouldAllowUnauthenticatedAccessToInfoEndpointOnManagementPort() {
    authMock.authorizeAnyRequest().deny();

    ResponseEntity<String> response =
        restTemplate.getForEntity(
            String.format("http://localhost:%d/actuator/info", managementPort), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldAllowUnauthenticatedAccessToMetricsEndpointOnManagementPort() {
    authMock.authorizeAnyRequest().deny();

    ResponseEntity<String> response =
        restTemplate.getForEntity(
            String.format("http://localhost:%d/actuator/metrics", managementPort), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldAllowUnauthenticatedAccessToPrometheusEndpointOnManagementPort() {
    authMock.authorizeAnyRequest().deny();

    ResponseEntity<String> response =
        restTemplate.getForEntity(
            String.format("http://localhost:%d/actuator/prometheus", managementPort), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldStillRequireAuthenticationForMcpEndpointsOnApplicationPort() {
    authMock.authorizeAnyRequest().deny();

    ResponseEntity<String> response =
        restTemplate.getForEntity(
            String.format("http://localhost:%d/sse", applicationPort), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldDenyAccessToActuatorEndpointsOnApplicationPort() {
    authMock.authorizeAnyRequest().deny();

    ResponseEntity<String> response =
        restTemplate.getForEntity(
            String.format("http://localhost:%d/actuator/health", applicationPort), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void shouldVerifyDifferentPortsAreUsed() {
    assertThat(managementPort).isNotEqualTo(applicationPort);
    assertThat(managementPort).isGreaterThan(0);
    assertThat(applicationPort).isGreaterThan(0);
  }

  @Test
  void listEndpoints() {
    System.out.println(
        webEndpointDiscoverer.getEndpoints().stream()
            .map(ExposableEndpoint::getEndpointId)
            .toList());
  }
}
