/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing.testcontainers.mongodb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * JUnit 5 extension annotation to configure MongoDB containers for test execution using
 * TestContainers.
 *
 * <p>This annotation is used in conjunction with the {@link TCMongoDBExtension} to automatically
 * start and manage a MongoDB container during test execution. It supports customization via the
 * {@code dockerImage} and {@code singleInstance} parameters.
 *
 * <p><b>Usage</b>
 *
 * <p>Apply this annotation to test classes, methods, or parameters (depending on the JUnit
 * extension lifecycle) to enable MongoDB container support for your tests.
 *
 * <p><b>Example</b>
 *
 * <p>
 *
 * <pre>{@code
 * @MongoDBTest(dockerImage = "mongo:8.0", singleInstance = true)
 * class MyMongoTest {
 *   // Test methods that use MongoDB
 * }
 * }</pre>
 *
 * <p>When {@code singleInstance = true}, a shared MongoDB container is reused across test classes.
 * Otherwise, each test will start its own MongoDB instance using the specified image.
 *
 * @see TCMongoDBExtension
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(TCMongoDBExtension.class)
public @interface MongoDBTest {

  /**
   * The Docker image name to use for the MongoDB container.
   *
   * <p>Defaults to "mongo:8.0".
   *
   * @return The Docker image name.
   */
  String dockerImage() default "mongo:8.0";

  /**
   * Whether to use a shared MongoDB container across tests.
   *
   * <p>If set to {@code true}, the same container will be reused for all tests that use this
   * annotation. Otherwise, a new container is started per test class.
   *
   * <p>Defaults to false.
   *
   * @return {@code true} to use a shared container, {@code false} otherwise.
   */
  boolean singleInstance() default false;
}
