/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getAllServeEvents;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.opentelemetry.sdk.testing.junit5.OpenTelemetryExtension;
import java.net.URI;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sdase.commons.spring.boot.web.monitoring.testing.MonitoringTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(
    classes = MonitoringTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "test.tracing.client.base.url=http://localhost:${wiremock.server.port}/feign",
      "opa.disable=true",
      "management.tracing.enabled=true",
      "management.tracing.grpc.enabled=true",
      "management.tracing.grpc.endpoint=http://localhost:${wiremock.server.port}"
    })
@AutoConfigureWireMock(port = 0)
@ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
@AutoConfigureObservability
class TracingFeignClientGrpcIT {

  @LocalServerPort private int port;

  @Autowired AuthMock authMock;

  RestTemplate client = new RestTemplate();

  @RegisterExtension
  @Order(0)
  static final OpenTelemetryExtension OTEL = OpenTelemetryExtension.create();

  @BeforeEach
  void setUp() {
    // WHEN
    reset();
    stubFor(get("/feign/pongMetrics").willReturn(ok()));
    stubFor(post("/opentelemetry.proto.collector.trace.v1.TraceService/Export").willReturn(ok()));

    authMock.reset();
    authMock.authorizeAnyRequest().allow();
  }

  @Test
  void shouldExtractTraceAndSpanForW3CContextPropagationAndUseInClient() {

    var result =
        client.exchange(
            RequestEntity.get(
                    URI.create(String.format("http://localhost:%d/api/pingMetrics", port)))
                .header("traceparent", "00-00000000000000000000000000000001-0000000000000002-01")
                .build(),
            String.class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> assertThat(getAllServeEvents()).hasSize(2));

    verify(
        getRequestedFor(urlPathEqualTo("/feign/pongMetrics"))
            .withHeader("traceparent", containing("00000000000000000000000000000001")));

    verify(
        postRequestedFor(
            urlPathEqualTo("/opentelemetry.proto.collector.trace.v1.TraceService/Export")));
  }

  @Test
  void shouldExtractTraceAndSpanForB3ContextPropagationAndUseInClient() {

    // WHEN
    stubFor(get("/feign/pong").willReturn(ok()));
    stubFor(post("/opentelemetry.proto.collector.trace.v1.TraceService/Export").willReturn(ok()));

    var result =
        client.exchange(
            RequestEntity.get(
                    URI.create(String.format("http://localhost:%d/api/pingMetrics", port)))
                .header("x-b3-traceid", "0000000000000001")
                .header("x-b3-spanid", "0000000000000002")
                .header("x-b3-sampled", "1")
                .build(),
            String.class);

    assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);

    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() -> assertThat(getAllServeEvents().stream()).hasSize(2));

    verify(
        getRequestedFor(urlPathEqualTo("/feign/pongMetrics"))
            .withHeader("b3", containing("0000000000000001"))
            .withHeader("uber-trace-id", containing("00000000000000000000000000000001")));

    verify(
        postRequestedFor(
            urlPathEqualTo("/opentelemetry.proto.collector.trace.v1.TraceService/Export")));
  }
}
