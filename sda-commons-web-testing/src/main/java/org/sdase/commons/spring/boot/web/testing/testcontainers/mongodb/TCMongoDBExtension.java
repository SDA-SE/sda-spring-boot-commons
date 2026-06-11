/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing.testcontainers.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.mongodb.MongoDBContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * JUnit 5 extension to manage the lifecycle of MongoDB containers for tests using TestContainers.
 *
 * <p>This extension starts a MongoDB container before all or each test, clears the database before
 * each individual test method, and shuts down the container after all tests have completed. The
 * configuration is determined via the {@link MongoDBTest} annotation on test classes or methods.
 *
 * <p><b>Lifecycle</b>
 *
 * <p>
 *
 * <ul>
 *   <li>{@link #beforeAll(ExtensionContext)}: Starts the MongoDB container and sets the connection
 *       string as a system property.
 *   <li>{@link #beforeEach(ExtensionContext)}: Truncates all collections in the database to reset
 *       state before each test.
 *   <li>{@link #afterAll(ExtensionContext)}: Shuts down the container if it was not configured as a
 *       shared resource.
 * </ul>
 *
 * <p><b>Important Notes</b>
 *
 * <p>
 *
 * <ul>
 *   <li>The container is started once per test class (if not shared) or globally if {@code
 *       singleInstance = true}.
 *   <li>The connection string is set to the system property "spring.mongodb.uri" so it can be used
 *       by Spring Boot applications.
 *   <li>The database is cleared before each test method to ensure isolation between tests.
 * </ul>
 */
public class TCMongoDBExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback {

  private static final Logger LOGGER = LoggerFactory.getLogger(TCMongoDBExtension.class);
  private static final String DATABASE_NAME = "testing";
  private MongoDBContainer mongoDBContainer = null;
  private boolean shutdown = true;

  /**
   * Starts the MongoDB container before all tests in a test class.
   *
   * <p>If the {@code MongoDBTest} annotation's {@code singleInstance} flag is set to {@code true},
   * this method reuses a shared container across test classes (via the global extension context).
   * Otherwise, it creates and starts a new container per test class.
   *
   * @param context The extension context used to retrieve the test class and configuration.
   */
  @Override
  public void beforeAll(ExtensionContext context) {
    MongoDBTest mongoDbTest = context.getRequiredTestClass().getAnnotation(MongoDBTest.class);

    if (mongoDbTest == null) {
      LOGGER.warn("Can't start testcontainer MongoDB because MongoDBTest was not configured.");
      return;
    }

    if (mongoDbTest.singleInstance()) {
      ExtensionContext.Store store = getContextStore(context);
      mongoDBContainer =
          (MongoDBContainer)
              store.computeIfAbsent(
                  "mongoDb",
                  key -> new MongoDBContainer(DockerImageName.parse("mongo:8.0")).withReplicaSet());
      shutdown = false;
    } else {
      mongoDBContainer =
          new MongoDBContainer(DockerImageName.parse(mongoDbTest.dockerImage())).withReplicaSet();
    }

    if (!mongoDBContainer.isRunning()) {
      mongoDBContainer.start();
      String connectionString = mongoDBContainer.getReplicaSetUrl(DATABASE_NAME);
      System.setProperty("spring.mongodb.uri", connectionString);
      LOGGER.info("Started testcontainer MongoDB under {}", connectionString);
    }
  }

  private ExtensionContext.@NonNull Store getContextStore(ExtensionContext context) {
    return context.getRoot().getStore(ExtensionContext.Namespace.GLOBAL);
  }

  /**
   * Clears all collections in the database before each test method.
   *
   * <p>This ensures that tests run in an isolated environment, preventing leftover data from prior
   * test executions.
   *
   * @param context The extension context.
   */
  @Override
  public void beforeEach(ExtensionContext context) {
    if (mongoDBContainer != null && mongoDBContainer.isRunning()) {
      try (MongoClient mongoClient =
          MongoClients.create(mongoDBContainer.getReplicaSetUrl(DATABASE_NAME))) {
        MongoDatabase database = mongoClient.getDatabase(DATABASE_NAME);
        database.listCollections().forEach(Document::clear);
      }
    }
  }

  /**
   * Shuts down the MongoDB container after all tests in a test class.
   *
   * <p>If the container was not configured as a shared resource (i.e., {@code singleInstance =
   * false}), it will be closed here to clean up resources.
   *
   * @param context The extension context.
   */
  @Override
  public void afterAll(ExtensionContext context) {
    if (shutdown && mongoDBContainer != null && mongoDBContainer.isRunning()) {
      mongoDBContainer.close();
      String connectionString = mongoDBContainer.getReplicaSetUrl(DATABASE_NAME);
      LOGGER.info("Shutdown testcontainer MongoDB under {}", connectionString);
    }
  }
}
