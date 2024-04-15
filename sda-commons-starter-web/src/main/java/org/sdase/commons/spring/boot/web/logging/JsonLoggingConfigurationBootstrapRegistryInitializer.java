/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.logging;

import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;

/**
 * Configures Spring Boot to log as JSON based on the SDA standard setting {@code
 * ENABLE_JSON_LOGGING}. This is implemented as {@link BootstrapRegistryInitializer} to cover as
 * many log statements as possible - including logs that are printed before bean processing is
 * completed. However, there are still a few logs of the early initialization phase that can't be
 * covered.
 */
public class JsonLoggingConfigurationBootstrapRegistryInitializer
    implements BootstrapRegistryInitializer {

  private static final String ENABLE_JSON_LOGGING_ENV_NAME = "ENABLE_JSON_LOGGING";
  private static final String ENABLE_JSON_LOGGING_PROPERTY_NAME = "enable.json.logging";
  private static final String LOG_DATEFORMAT_PATTERN_ENV_NAME = "LOG_DATEFORMAT_PATTERN";
  private static final String LOG_DATEFORMAT_PATTERN_PROPERTY_NAME = "log.dateformat.pattern";
  private static final String DEFAULT_LOG_DATEFORMAT_PATTERN = "yyyy-MM-dd HH:mm:ss.SSS";
  private static final String LOGGING_CONFIG_PROPERTY_NAME = "logging.config";
  private static final String JSON_LOGGING_CONFIG_RESOURCE =
      "classpath:org/sdase/commons/spring/boot/web/logging/logback-json.xml";

  @Override
  public void initialize(BootstrapRegistry registry) {
    if (isJsonLoggingEnabled()) {
      System.setProperty("JSON_DATEFORMAT_PATTERN", dateFormatPattern());
      configureJsonLogging();
    }
  }

  private void configureJsonLogging() {
    System.setProperty(LOGGING_CONFIG_PROPERTY_NAME, JSON_LOGGING_CONFIG_RESOURCE);
  }

  private boolean isJsonLoggingEnabled() {
    return isJsonLoggingEnabledByEnv() || isJsonLoggingEnabledByProperty();
  }

  private boolean isJsonLoggingEnabledByEnv() {
    return isTrue(System.getenv(ENABLE_JSON_LOGGING_ENV_NAME));
  }

  private boolean isJsonLoggingEnabledByProperty() {
    return isTrue(System.getProperty(ENABLE_JSON_LOGGING_PROPERTY_NAME));
  }

  private String dateFormatPattern() {
    if (System.getenv(LOG_DATEFORMAT_PATTERN_ENV_NAME) != null) {
      return System.getenv(LOG_DATEFORMAT_PATTERN_ENV_NAME);
    }
    if (System.getProperty(LOG_DATEFORMAT_PATTERN_PROPERTY_NAME) != null) {
      return System.getProperty(LOG_DATEFORMAT_PATTERN_PROPERTY_NAME);
    }
    return DEFAULT_LOG_DATEFORMAT_PATTERN;
  }

  private boolean isTrue(String value) {
    return Boolean.TRUE.equals(Boolean.parseBoolean(value));
  }
}
