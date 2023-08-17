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
import java.util.Map.Entry;
import org.springframework.boot.actuate.autoconfigure.tracing.otlp.OtlpProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:/org/sdase/commons/spring/boot/web/monitoring/tracing.properties")
@AutoConfiguration
@EnableConfigurationProperties(OtlpProperties.class)
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

  /**
   * creates the gRPC exporter for Open Telemetry. It will be conditionally enabled on property
   * management.tracing.grpc.enabled=true. It will replace the http exporter.
   *
   * @param properties the {@link OtlpProperties} configuration properties
   * @return an instance of {@link OtlpGrpcSpanExporter}
   */
  @Bean
  @ConditionalOnProperty(value = "management.tracing.grpc.enabled", havingValue = "true")
  public OtlpGrpcSpanExporter otlpGrpcExporter(OtlpProperties properties) {
    var builder =
        OtlpGrpcSpanExporter.builder()
            .setEndpoint(properties.getEndpoint())
            .setCompression(String.valueOf(properties.getCompression()).toLowerCase())
            .setTimeout(properties.getTimeout());
    for (Entry<String, String> header : properties.getHeaders().entrySet()) {
      builder.addHeader(header.getKey(), header.getValue());
    }
    return builder.build();
  }
}
