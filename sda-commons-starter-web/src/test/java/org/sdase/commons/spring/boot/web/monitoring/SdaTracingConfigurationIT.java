/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.monitoring.testing.MonitoringTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestPropertySource;

class SdaTracingConfigurationIT {

  @SpringBootTest(classes = MonitoringTestApp.class)
  @TestPropertySource(
      properties = {
        "management.tracing.grpc.enabled=true",
        "management.opentelemetry.tracing.export.otlp.endpoint=http://localhost:4317",
        "test.tracing.client.base.url=http://localhost:${wiremock.server.port}/feign"
      })
  static class EnabledWithUrlTest {

    @Autowired ApplicationContext context;

    @Test
    void beanIsRegistered() {
      assertThat(context.getBeansOfType(OtlpGrpcSpanExporter.class)).hasSize(1);
    }
  }

  @SpringBootTest(classes = MonitoringTestApp.class)
  @TestPropertySource(
      properties = {
        "management.tracing.grpc.enabled=true",
        "test.tracing.client.base.url=http://localhost:${wiremock.server.port}/feign",
        "management.opentelemetry.tracing.export.otlp.endpoint="
      })
  static class EnabledWithoutUrlTest {

    @Autowired ApplicationContext context;

    @Test
    void beanIsRegistered() {
      assertThat(context.getBeansOfType(OtlpGrpcSpanExporter.class)).hasSize(1);
    }
  }

  @SpringBootTest(classes = MonitoringTestApp.class)
  @TestPropertySource(
      properties = {
        "management.tracing.grpc.enabled=false",
        "test.tracing.client.base.url=http://localhost:${wiremock.server.port}/feign"
      })
  static class NotEnabledTest {

    @Autowired ApplicationContext context;

    @Test
    void beanIsNotRegistered() {
      assertThat(context.getBeansOfType(OtlpGrpcSpanExporter.class)).isEmpty();
    }
  }
}
