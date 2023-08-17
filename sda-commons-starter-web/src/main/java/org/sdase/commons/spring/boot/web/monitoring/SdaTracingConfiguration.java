/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:/org/sdase/commons/spring/boot/web/monitoring/tracing.properties")
@AutoConfiguration
public class SdaTracingConfiguration {

  @ConditionalOnProperty(value = "management.tracing.enabled", havingValue = "false")
  @Bean
  public OpenTelemetry openTelemetry() {
    return OpenTelemetry.noop();
  }

  /**
   * adding support for jaeger type propagation
   *
   * @return jaeger propagator instance
   */
  @Bean
  public TextMapPropagator jaegerPropagator() {
    return JaegerPropagator.getInstance();
  }

  @Bean
  @ConditionalOnProperty(value = "management.tracing.grpc.enabled", havingValue = "true")
  public OtlpGrpcSpanExporter otlpGrpcExporter(
      @Value("${management.tracing.grpc.endpoint}") String endpoint,
      @Value("${management.tracing.grpc.compression}") String compression,
      @Value("${management.tracing.grpc.timeoutMs}") Long timeout) {
    var builder = OtlpGrpcSpanExporter.builder();
    builder.setEndpoint(endpoint);
    builder.setCompression(compression);
    builder.setTimeout(Duration.ofMillis(timeout));
    return builder.build();
  }
}
