/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.logging;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sdase.commons.spring.boot.web.jackson.test.JacksonTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = JacksonTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"management.server.port=8081"})
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
@ExtendWith(OutputCaptureExtension.class)
@AutoConfigureTestRestTemplate
class HttpRequestLoggingTest {

  @LocalServerPort private int port;
  @LocalManagementPort private int managementPort;

  @Autowired private TestRestTemplate client;

  @Test
  void shouldLogRegularRequest(CapturedOutput output) {
    // when
    ResponseEntity<String> responseEntity = executeRequest("/api/fixedTime", port);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    var expectedLogLine = findLogLine(output);
    assertThat(expectedLogLine).isPresent();
    assertThat(expectedLogLine.get()).contains("GET /api/fixedTime");
  }

  @Test
  void shouldNotLogMetricsEndpoints(CapturedOutput output) {
    // when
    ResponseEntity<String> responseEntity =
        executeRequest("/healthcheck/readiness", managementPort);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
    var expectedLogLine = findLogLine(output);
    assertThat(expectedLogLine).isEmpty();
  }

  private Optional<String> findLogLine(CapturedOutput output) {
    return output.toString().lines().toList().stream()
        .filter(line -> line.contains("HttpRequestLoggingInterceptor"))
        .findFirst();
  }

  private ResponseEntity<String> executeRequest(String path, int port) {
    return client.getForEntity(String.format("http://localhost:%d%s", port, path), String.class);
  }
}
