/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi;

import com.fasterxml.jackson.databind.JsonNode;

public interface FinalBuilder {
  /**
   * Generates a new JSON schema for the supplied class.
   *
   * @return A JSON object for the JSON schema.
   */
  JsonNode generate();

  /**
   * Generates a new JSON schema for the supplied class.
   *
   * @return A YAML representation for the JSON schema.
   */
  String generateYaml();
}
