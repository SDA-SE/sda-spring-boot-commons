/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi;

import org.sdase.commons.spring.boot.asyncapi.exception.JacksonYamlException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

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
   * @throws JacksonYamlException if json processing fails
   */
  default String generateYaml() {
    try {
      return YAMLMapper.builder().build().writeValueAsString(generate());
    } catch (JacksonException e) {
      throw new JacksonYamlException("Error while converting JSON to YAML.", e);
    }
  }
}
