/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing.testcontainers.kafka;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * JUnit 5 extension annotation to configure Kafka containers for test execution using
 * TestContainers.
 *
 * <p>This annotation is used in conjunction with the {@link TCKafkaExtension} to automatically
 * start and manage a Kafka container during test execution. It supports customization via the
 * {@code dockerImage} and {@code singleInstance} parameters.
 *
 * <p><b>Usage</b>
 *
 * <p>Apply this annotation to test classes, methods, or parameters (depending on the JUnit
 * extension lifecycle) to enable Kafka container support for your tests.
 *
 * <p><b>Example</b>
 *
 * <p>
 *
 * <pre>{@code
 * @KafkaTest(dockerImage = "apache/kafka-native:4.2.0", singleInstance = true)
 * class MyKafkaTest {
 *   // Test methods that use Kafka
 * }
 * }</pre>
 *
 * <p>When {@code singleInstance = true}, a shared Kafka container is reused across test classes.
 * Otherwise, each test will start its own Kafka instance using the specified image.
 *
 * @see TCKafkaExtension
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(TCKafkaExtension.class)
public @interface KafkaTest {

  /**
   * The Docker image name to use for the Kafka container.
   *
   * <p>Defaults to "apache/kafka-native:4.2.0".
   *
   * @return The Docker image name.
   */
  String dockerImage() default "apache/kafka-native:4.2.0";

  /**
   * Whether to use a shared Kafka container across tests.
   *
   * <p>If set to {@code true}, the same container will be reused for all tests that use this
   * annotation. Otherwise, a new container is started per test class.
   *
   * <p>Defaults to true.
   *
   * @return {@code true} to use a shared container, {@code false} otherwise.
   */
  boolean singleInstance() default true;
}
