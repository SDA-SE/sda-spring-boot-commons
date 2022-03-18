/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.auth.opa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

public class OpaResponse {
  private static final String ALLOW = "allow";

  private JsonNode result;

  public JsonNode getResult() {
    return result;
  }

  public OpaResponse setResult(JsonNode result) {
    this.result = result;
    return this;
  }

  @JsonIgnore()
  public boolean isAllow() {
    if (result.has(ALLOW) && result.get(ALLOW).isBoolean()) {
      return result.get(ALLOW).asBoolean();
    }
    return false;
  }
}
