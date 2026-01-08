/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.tracing;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.matching;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.sdase.commons.spring.boot.web.tracing.test.TracingTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.wiremock.spring.ConfigureWireMock;
import org.wiremock.spring.EnableWireMock;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@SetSystemProperty(key = "enable.json.logging", value = "true")
@SpringBootTest(
    classes = TracingTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "otherServiceClient.baseUrl=http://localhost:${wiremock.server.port}/api",
    })
@EnableWireMock({@ConfigureWireMock(port = 0)})
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
@ExtendWith(OutputCaptureExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
@AutoConfigureTestRestTemplate
class TraceTokenTest {
  @LocalServerPort private int port;
  @Autowired private TestRestTemplate client;
  @Autowired ObjectMapper objectMapper;
  static final TypeReference<Map<String, Object>> MAP_REF = new TypeReference<>() {};

  @BeforeEach
  void setUp() {
    WireMock.reset();
  }

  @Test
  void shouldPreserveTraceTokenIfPresent(CapturedOutput output) {
    // given
    var headers = new HttpHeaders();
    headers.set("Trace-Token", "pre-defined-trace-token");

    // when
    ResponseEntity<String> responseEntity = executeRequestWithHeader("/api/static", port, headers);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    // response should have same trace-token header
    assertThat(responseEntity.getHeaders().getFirst("Trace-Token"))
        .isEqualTo("pre-defined-trace-token");

    // mdc should contain same trace-token
    final AtomicReference<Optional<String>> expectedLogLine = new AtomicReference<>();

    await()
        .untilAsserted(
            () -> {
              expectedLogLine.set(findLogLine(output));
              assertThat(expectedLogLine.get()).isPresent();
            });

    var actualLogLine = expectedLogLine.get().get();
    var mdc = (Map<String, String>) toObject(actualLogLine).get("mdc");

    assertThat(mdc)
        .extracting("requestHeaderTraceToken", "responseHeaderTraceToken")
        .containsExactly("pre-defined-trace-token", "pre-defined-trace-token");
  }

  @Test
  void shouldGenerateTraceTokenIfAbsent(CapturedOutput output) {
    // when
    ResponseEntity<String> responseEntity =
        executeRequestWithHeader("/api/static", port, new HttpHeaders());

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);

    // response should have not-blank trace-token header
    var generatedTraceToken = responseEntity.getHeaders().getFirst("Trace-Token");
    assertThat(generatedTraceToken).isNotBlank();
    assertValidUUID(generatedTraceToken);

    // mdc should contain same trace-token
    final AtomicReference<Optional<String>> expectedLogLine = new AtomicReference<>();

    await()
        .untilAsserted(
            () -> {
              expectedLogLine.set(findLogLine(output));
              assertThat(expectedLogLine.get()).isPresent();
            });

    var actualLogLine = expectedLogLine.get().get();
    var mdc = (Map<String, String>) toObject(actualLogLine).get("mdc");

    assertThat(mdc)
        .extracting("requestHeaderTraceToken", "responseHeaderTraceToken")
        .containsExactly(null, generatedTraceToken);
  }

  @Test
  void shouldAddExistingTraceTokenToOutgoingRequests() {
    // stub and verify the Trace-Token
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/hello"))
            .withHeader("Trace-Token", equalTo("pre-defined-trace-token"))
            .willReturn(ResponseDefinitionBuilder.okForJson(Map.of("hello", "world"))));

    var headers = new HttpHeaders();
    headers.set("Trace-Token", "pre-defined-trace-token");

    // when
    ResponseEntity<String> responseEntity = executeRequestWithHeader("/api/proxy", port, headers);

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void shouldAddGeneratedTraceTokenToOutgoingRequests() {
    String uuidRegex =
        "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";

    // stub and verify the Trace-Token
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/api/hello"))
            .withHeader("Trace-Token", matching(uuidRegex))
            .willReturn(ResponseDefinitionBuilder.okForJson(Map.of("hello", "world"))));

    // when
    ResponseEntity<String> responseEntity =
        executeRequestWithHeader("/api/proxy", port, new HttpHeaders());

    // then
    assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  private void assertValidUUID(String uuid) {
    assertThat(UUID.fromString(uuid).toString()).hasToString(uuid);
  }

  private ResponseEntity<String> executeRequestWithHeader(
      String path, int port, HttpHeaders headers) {
    return client.exchange(
        String.format("http://localhost:%d%s", port, path),
        HttpMethod.GET,
        new HttpEntity<>(headers),
        String.class);
  }

  private Optional<String> findLogLine(CapturedOutput output) {
    return output.toString().lines().toList().stream()
        .filter(line -> line.contains("HttpRequestLoggingInterceptor"))
        .findFirst();
  }

  private Map<String, Object> toObject(String json) {
    return objectMapper.readValue(json, MAP_REF);
  }
}
