/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Mapped {@link com.fasterxml.jackson.databind.JsonNode} to {@link tools.jackson.databind.JsonNode}
 *
 * @deprecated gets removed, when victools jsonschema-generator uses Jackson 3
 */
@Deprecated(since = "7.0.0")
public class Jackson2To3Bridge {

  private final ObjectMapper jackson2;
  private final JsonMapper jackson3;

  public Jackson2To3Bridge() {
    this.jackson2 = new ObjectMapper();
    this.jackson3 = JsonMapper.builderWithJackson2Defaults().build();
  }

  /** Convert any Jackson 2 JsonNode to Jackson 3 JsonNode via JSON bytes. */
  public tools.jackson.databind.JsonNode toJackson3(
      com.fasterxml.jackson.databind.JsonNode jackson2Node) {
    if (jackson2Node == null) return null;

    try {
      byte[] json = jackson2.writeValueAsBytes(jackson2Node);
      return jackson3.readTree(json);
    } catch (Exception e) {
      throw new IllegalStateException("Failed to migrate JsonNode from Jackson 2 to 3", e);
    }
  }
}
