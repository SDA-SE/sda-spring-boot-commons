/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.auth.opa.model;

import com.fasterxml.jackson.databind.JsonNode;

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
