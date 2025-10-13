/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth.opa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

public record OpaResponse(JsonNode result) {

  private static final String ALLOW = "allow";

  @JsonIgnore()
  public boolean isAllow() {
    if (result != null && result.has(ALLOW) && result.get(ALLOW).isBoolean()) {
      return result.get(ALLOW).asBoolean();
    }
    return false;
  }
}
