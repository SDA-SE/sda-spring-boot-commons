/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.boot.actuate.availability.ReadinessStateHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.mongo.MongoHealthIndicator;
import org.springframework.context.annotation.Import;

/**
 *
 *
 * <h2>Monitoring </h2>
 *
 * Enables features that make a Spring Boot service compliant with the <a
 * href="https://sda.dev/developer-guide/deployment/health-checks/">SDA SE Health Checks</a>.
 *
 * <p>Configures the Spring Boot Actuator to be accessible on root path {@code "/"} at default
 * management port {@code 8081}.
 *
 * <p>Request for healtchecks on {@code /healthcheck/{*pathToIndicatorOrGroup}"}
 *
 * <ul>
 *   <li>Request for liveness probe on {@code "/healthcheck/liveness"}
 *   <li>Request for readiness probe on {@code "/healthcheck/readiniess"}.
 * </ul>
 *
 * <p>The readiness group contains the following indicators:
 *
 * <ul>
 *   <li>{@linkplain ReadinessStateHealthIndicator}
 *   <li>{@linkplain MongoHealthIndicator}, if auto-configured.
 * </ul>
 *
 * To overwrite the defaults {@link HealthIndicator} of the readiness group, you can overwrite the
 * property source {@code "management.endpoint.health.group.readiness.include=readinessState,
 * customCheck"}.
 *
 * <p>Custom health indicators can be easily added to the application context:
 *
 * <pre>
 *     <code>{@literal @}Component public class CustomHealthIndicator implements HealthIndicator {
 *              {@literal @}Override
 *                      public Health health() {
 *                          return new Health.Builder().up().build();
 *                      }
 *                  }
 *     </code>
 *   </pre>
 *
 * The custom health indicator will be available under {@code "/healthcheck/custom"} which is
 * resolved by the prefix of the {@linkplain HealthIndicator} implementing component.
 *
 * <p>TODO METRIC / PROMETHEUS
 *
 * <h2>Tracing</h2>
 *
 * <p>Currently, tracing is leveraged by Sleuth in the Spring context. Spring Cloud Sleuth provides
 * Spring Boot auto-configuration for distributed tracing. Sleuth was built around Zipkin traces and
 * so only supports forwarding them to Zipkin (Thrift via Brave) format for now. But since Jaeger
 * supports Zipkin traces and the OpenTracing Jaager Spring support is not heavily maintained, there
 * is a need to stick with Sleuth. However, Spring Sleuth is compatible with OpenTracing, so we can
 * use the standardized interfaces, hence the OpenTracing {@linkplain io.opentracing.Tracer} is on
 * classpath.
 *
 * <p>Even if Jaeger supports the Zipkin B3 propagation format, Sleuth is forced to just use per
 * default the <a href="https://www.w3.org/TR/trace-context">W3C context propagation</a>
 *
 * <p>Default features are:
 *
 * <ul>
 *   <li>Adds trace and span ids to the Slf4J MDC, so you can extract all the logs from a given
 *       trace or span in a log aggregator.
 *   <li>Instruments common ingress and egress points from Spring applications (servlet filter, rest
 *       template, scheduled actions, message channels, feign client).
 *   <li>The service name is derived from {@code spring.application.name}
 *   <li>Generate and report Jaeger-compatible traces via HTTP. By default it sends them to a Zipkin
 *       collector on localhost (port 9411). Configure the location of the service using {@code
 *       spring.zipkin.base-url}
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({SdaMonitoringConfiguration.class, SdaTracingConfiguration.class})
public @interface EnableSdaMonitoring {}
