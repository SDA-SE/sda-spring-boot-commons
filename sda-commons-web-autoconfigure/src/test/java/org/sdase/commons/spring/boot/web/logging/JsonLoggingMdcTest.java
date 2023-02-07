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
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.sdase.commons.spring.boot.web.jackson.test.JacksonTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;

@SetSystemProperty(key = "enable.json.logging", value = "true")
@SpringBootTest(
    classes = JacksonTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
@ExtendWith(OutputCaptureExtension.class)
class JsonLoggingMdcTest {

  @LocalServerPort int port;
  @Autowired TestRestTemplate client;
  @Autowired ObjectMapper objectMapper;
  static final TypeReference<Map<String, Object>> MAP_REF = new TypeReference<>() {};

  @Test
  void shouldLogJsonWithMdc(CapturedOutput capturedOutput) {
    Logger log = LoggerFactory.getLogger(JsonLoggingMdcTest.class);
    try (var ignored = MDC.putCloseable("test-key", "test-value")) {
      log.info("Hello from the test.");
      var actual =
          toStructuredLogs(capturedOutput).stream()
              .filter(l -> getClass().getName().equals(l.get("logger")))
              .findFirst();

      assertThat(actual)
          .isPresent()
          .get()
          .extracting("mdc")
          .extracting("test-key")
          .isEqualTo("test-value");
    }
  }

  @Test
  void shouldLogJsonWithMdcInRequestContext(CapturedOutput capturedOutput) {
    doRequest();

    var actual = toStructuredLogs(capturedOutput);

    assertThat(actual)
        .anySatisfy(
            log ->
                assertThat(log)
                    .extracting("mdc")
                    .asInstanceOf(InstanceOfAssertFactories.MAP)
                    .isNotNull()
                    .isNotEmpty());
  }

  @Test
  @Disabled("No trace token support implemented yet: PLP-655")
  void shouldLogJsonWithTraceTokenInMdc(CapturedOutput capturedOutput) {
    doRequest();

    var actual = toStructuredLogs(capturedOutput);

    assertThat(actual)
        .anySatisfy(
            log ->
                assertThat(log)
                    .extracting("mdc")
                    .extracting("Trace-Token")
                    .asString()
                    .isNotBlank());
  }

  private List<Map<String, Object>> toStructuredLogs(CapturedOutput capturedOutput) {
    return capturedOutput
        .toString()
        .lines()
        .filter(l -> l.startsWith("{"))
        .map(this::toObject)
        .toList();
  }

  private void doRequest() {
    client.getForObject("http://localhost:" + port + "/api/fixedTime", Object.class);
  }

  private Map<String, Object> toObject(String json) {
    try {
      return objectMapper.readValue(json, MAP_REF);
    } catch (JsonProcessingException e) {
      throw new UncheckedIOException(e);
    }
  }
}
