/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.testing.auth.opa.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

public class OpaMockResponse {
  private static final String ALLOW = "allow";

  private JsonNode result;

  public JsonNode getResult() {
    return result;
  }

  public OpaMockResponse setResult(JsonNode result) {
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
