/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing.testcontainers.kafka;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * JUnit 5 extension to manage the lifecycle of Kafka containers for tests using TestContainers.
 *
 * <p>This extension starts a Kafka container before all tests in a test class and shuts it down
 * after all tests have completed. It supports shared container reuse when the {@code
 * singleInstance} flag is enabled on the test class or method.
 *
 * <p><b>Lifecycle</b>
 *
 * <p>
 *
 * <ul>
 *   <li>{@link #beforeAll(ExtensionContext)}: Starts the Kafka container and sets the system
 *       property "spring.kafka.bootstrap-servers" with its bootstrap servers address.
 *   <li>{@link #afterAll(ExtensionContext)}: Shuts down the container if it was not configured as a
 *       shared resource.
 * </ul>
 *
 * <p><b>Important Notes</b>
 *
 * <p>
 *
 * <ul>
 *   <li>If {@code singleInstance = true} (the default), the container is globally shared across all
 *       tests and will <strong>not</strong> be closed after tests.
 *   <li>If {@code singleInstance = false}, the container will be closed after all tests to ensure
 *       resource cleanup.
 *   <li>The system property "spring.kafka.bootstrap-servers" is set to the container's bootstrap
 *       servers URL, allowing Spring Boot-based applications to connect automatically.
 * </ul>
 */
public class TCKafkaExtension implements BeforeAllCallback, AfterAllCallback {

  private static final Logger LOGGER = LoggerFactory.getLogger(TCKafkaExtension.class);

  private KafkaContainer kafkaContainer = null;
  private boolean shutdown = false;

  /**
   * Starts the Kafka container before all tests in a test class.
   *
   * <p>If the {@code KafkaTest} annotation's {@code singleInstance} flag is set to {@code true},
   * this method reuses a shared container across test classes (via the global extension context).
   * Otherwise, it creates and starts a new container per test class.
   *
   * @param context The extension context used to retrieve the test class and configuration.
   */
  @Override
  public void beforeAll(ExtensionContext context) {
    KafkaTest kafkaTest = context.getRequiredTestClass().getAnnotation(KafkaTest.class);

    if (kafkaTest == null) {
      LOGGER.warn("Can't start testcontainer Kafka because KafkaTest was not configured.");
      return;
    }

    if (kafkaTest.singleInstance()) {
      ExtensionContext.Store store = getContextStore(context);
      kafkaContainer =
          (KafkaContainer)
              store.computeIfAbsent(
                  "kafkaContainer",
                  key -> new KafkaContainer(DockerImageName.parse(kafkaTest.dockerImage())));
    } else {
      kafkaContainer = new KafkaContainer(DockerImageName.parse(kafkaTest.dockerImage()));
      shutdown = true;
    }

    if (!kafkaContainer.isRunning()) {
      kafkaContainer.start();
      System.setProperty("spring.kafka.bootstrap-servers", kafkaContainer.getBootstrapServers());
      LOGGER.info(
          "Started testcontainer Kafka with servers {}", kafkaContainer.getBootstrapServers());
    }
  }

  private ExtensionContext.@NonNull Store getContextStore(ExtensionContext context) {
    return context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
  }

  /**
   * Shuts down the Kafka container after all tests in a test class.
   *
   * <p>If the container was not configured as a shared resource (i.e., {@code singleInstance =
   * false}), it will be closed here to clean up resources.
   *
   * @param context The extension context.
   */
  @Override
  public void afterAll(ExtensionContext context) {
    if (shutdown && kafkaContainer != null && kafkaContainer.isRunning()) {
      kafkaContainer.close();
      LOGGER.info(
          "Shutdown testcontainer Kafka with servers {}", kafkaContainer.getBootstrapServers());
    }
  }
}
