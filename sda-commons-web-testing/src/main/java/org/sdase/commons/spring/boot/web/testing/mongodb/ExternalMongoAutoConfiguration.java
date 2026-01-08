/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing.mongodb;

import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.EnvironmentPostProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

public class ExternalMongoAutoConfiguration implements EnvironmentPostProcessor {
  private static final String FLAPDOODLE_AUTO_CONFIG_CLASS =
      "de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration";
  private static final String TEST_MONGODB_CONNECTION_STRING_PROPERTY_NAME =
      "test.mongodb.connection.string";
  private static final String AUTOCONFIGURE_EXCLUDES_PROPERTY_NAME = "spring.autoconfigure.exclude";
  private static final String SPRING_DATA_MONGODB_URI_PROPERTY_NAME = "spring.mongodb.uri";

  private static final Logger LOG = LoggerFactory.getLogger(ExternalMongoAutoConfiguration.class);

  @Override
  public void postProcessEnvironment(
      ConfigurableEnvironment environment, SpringApplication application) {
    String mongoDbUri = environment.getProperty(TEST_MONGODB_CONNECTION_STRING_PROPERTY_NAME);
    if (mongoDbUri != null) {
      try {
        // fails if Flapdoodle embedded MongoDB is not available
        getClass().getClassLoader().loadClass(FLAPDOODLE_AUTO_CONFIG_CLASS);
        // not logging the value, it may contain credentials
        LOG.info(
            "Disabling {} and using MongoDB from {} for tests.",
            FLAPDOODLE_AUTO_CONFIG_CLASS,
            TEST_MONGODB_CONNECTION_STRING_PROPERTY_NAME);
        excludeFlapdoodle(environment);
        setTestMongoUri(environment, mongoDbUri);
      } catch (ClassNotFoundException ignored) {
        LOG.info(
            "Not applying MongoDB defined with {}: "
                + "Assuming no MongoDB tests because {} is not in the classpath.",
            TEST_MONGODB_CONNECTION_STRING_PROPERTY_NAME,
            FLAPDOODLE_AUTO_CONFIG_CLASS);
      }
    }
  }

  private static void excludeFlapdoodle(ConfigurableEnvironment environment) {
    String excludes = environment.getProperty(AUTOCONFIGURE_EXCLUDES_PROPERTY_NAME);
    if (StringUtils.isBlank(excludes)) {
      excludes = FLAPDOODLE_AUTO_CONFIG_CLASS;
    } else {
      excludes = excludes + "," + FLAPDOODLE_AUTO_CONFIG_CLASS;
    }
    environment
        .getPropertySources()
        .addFirst(
            new MapPropertySource(
                "ExternalMongoEnvironmentPropertiesSource",
                Map.of(AUTOCONFIGURE_EXCLUDES_PROPERTY_NAME, excludes)));
  }

  private static void setTestMongoUri(ConfigurableEnvironment environment, String mongoDbUri) {
    environment
        .getPropertySources()
        .addFirst(
            new MapPropertySource(
                "ExternalMongoTestUriPropertiesSource",
                Map.of(SPRING_DATA_MONGODB_URI_PROPERTY_NAME, mongoDbUri)));
  }
}
