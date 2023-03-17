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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.sdase.commons.spring.boot.web.security.test.ContextUtils;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@ExtendWith(OutputCaptureExtension.class)
@ClearSystemProperty(key = "logging.config") // defined by app under test to enable json logging
@SetSystemProperty(key = "opa.disable", value = "true")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
class JsonLoggingTest {

  @Test
  void shouldLogRegular(CapturedOutput output) {
    assertThat(ContextUtils.createTestContext(LoggingTestApp.class)).hasNotFailed();

    assertThat(output).asString().contains("Started JsonLoggingTest.LoggingTestApp");
    assertThat(nonTestLogLines(output))
        .as("Log contains no JSON:\n{}", output.toString())
        .asList()
        .noneMatch(l -> l.toString().startsWith("{"));
  }

  @Test
  @SetSystemProperty(key = "enable.json.logging", value = "true")
  void shouldLogJson(CapturedOutput output) throws JsonProcessingException {
    assertThat(ContextUtils.createTestContext(LoggingTestApp.class)).hasNotFailed();
    assertThat(output).asString().contains("Started JsonLoggingTest.LoggingTestApp");
    for (String json : jsonLines(output)) {
      assertThat(new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {}))
          .containsKeys("level", "logger", "timestamp", "message");
    }
    assertThat(onlyConfigurableLogLines(nonTestLogLines(output)))
        .as("Log contains JSON:\n{}", output.toString())
        .asList()
        .isNotEmpty()
        .allMatch(l -> l.toString().startsWith("{"));
  }

  private List<String> nonTestLogLines(CapturedOutput capturedOutput) {
    return capturedOutput
        .toString()
        .lines()
        .filter(l -> !l.contains(".test.context."))
        .filter(l -> !l.contains("Test worker"))
        .toList();
  }

  private List<String> jsonLines(CapturedOutput capturedOutput) {
    return capturedOutput.toString().lines().filter(l -> l.startsWith("{")).toList();
  }

  private List<String> onlyConfigurableLogLines(List<String> logLines) {
    return logLines.stream()
        .filter(
            l ->
                !l.contains(
                    "org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider - Identified candidate component class"))
        .toList();
  }

  @SpringBootApplication
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  @ContextConfiguration
  public static class LoggingTestApp {}
}
