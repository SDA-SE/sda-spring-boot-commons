/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.logging;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.sdase.commons.spring.boot.web.jackson.test.JacksonTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SetSystemProperty(key = "enable.json.logging", value = "true")
@SpringBootTest(
    classes = JacksonTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
@ExtendWith(OutputCaptureExtension.class)
class HttpRequestLoggingJsonTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @Test
  void shouldLogAsJsonWhenEnabled(CapturedOutput output) throws JsonProcessingException {
    ResponseEntity<String> responseEntity = executeRequest("/api/fixedTime", port);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    // logs should be in json format
    for (String json : jsonLines(output)) {
      assertThat(new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {}))
          .containsKeys("level", "logger", "timestamp", "message");
    }

    // log for request should still be found
    var expectedLogLine = findLogLine(output);
    assertThat(expectedLogLine).isPresent();
    assertThat(expectedLogLine.get()).contains("GET /api/fixedTime");
  }

  private Optional<String> findLogLine(CapturedOutput output) {
    return output.toString().lines().toList().stream()
        .filter(line -> line.contains("HttpRequestLoggingInterceptor"))
        .findFirst();
  }

  private List<String> jsonLines(CapturedOutput capturedOutput) {
    return capturedOutput.toString().lines().filter(l -> l.startsWith("{")).toList();
  }

  private ResponseEntity<String> executeRequest(String path, int port) {
    return client.getForEntity(String.format("http://localhost:%d%s", port, path), String.class);
  }
}
