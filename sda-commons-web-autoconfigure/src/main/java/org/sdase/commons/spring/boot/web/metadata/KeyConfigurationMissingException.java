/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata;

/** Exception to be thrown when a metadata key can't be resolved from the configuration. */
public class KeyConfigurationMissingException extends RuntimeException {

  private final String unknownConfigurationKey;

  KeyConfigurationMissingException(String unknownConfigurationKey) {
    super(String.format("Could not find %s in configuration", unknownConfigurationKey));
    this.unknownConfigurationKey = unknownConfigurationKey;
  }

  /**
   * @return the configuration key that could not be found in {@link System#getProperty(String)} or
   *     {@link System#getenv(String)}.
   */
  public String getUnknownConfigurationKey() {
    return unknownConfigurationKey;
  }
}
