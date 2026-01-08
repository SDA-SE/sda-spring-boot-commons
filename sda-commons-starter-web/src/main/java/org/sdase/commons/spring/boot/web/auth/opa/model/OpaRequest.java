/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.opa.model;

import tools.jackson.databind.JsonNode;

public class OpaRequest {

  private final JsonNode input;

  private OpaRequest(JsonNode input) {
    this.input = input;
  }

  public JsonNode getInput() {
    return input;
  }

  public static OpaRequest request(JsonNode input) {
    return new OpaRequest(input);
  }
}
