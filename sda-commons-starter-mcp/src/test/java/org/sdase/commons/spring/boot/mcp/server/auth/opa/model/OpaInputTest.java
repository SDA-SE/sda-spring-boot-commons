/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth.opa.model;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class OpaInputTest {
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void testEqualsAndHashCode() throws Exception {
    JsonNode body1 = objectMapper.readTree("{\"key\":\"value\"}");
    JsonNode body2 = objectMapper.readTree("{\"key\":\"value\"}");
    String[] path = {"api", "v1", "resource"};
    OpaInput input1 = new OpaInput("jwtToken", path, "GET", "trace123", body1);
    OpaInput input2 = new OpaInput("jwtToken", path, "GET", "trace123", body2);
    assertThat(input1).isEqualTo(input2);
    assertThat(input1.hashCode()).hasSameHashCodeAs(input2.hashCode());
  }

  @Test
  void testNotEquals() throws Exception {
    JsonNode body = objectMapper.readTree("{\"key\":\"value\"}");
    String[] path = {"api", "v1", "resource"};
    OpaInput input1 = new OpaInput("jwtToken", path, "GET", "trace123", body);
    OpaInput input2 = new OpaInput("otherJwt", path, "GET", "trace123", body);
    assertThat(input1).isNotEqualTo(input2);
  }

  @Test
  void testToStringFormat() throws Exception {
    JsonNode body = objectMapper.readTree("{\"key\":\"value\"}");
    String[] path = {"api", "v1", "resource"};
    OpaInput input = new OpaInput("jwtToken", path, "GET", "trace123", body);
    String str = input.toString();
    assertThat(str).contains("jwtToken", "api", "GET", "trace123", "key");
  }
}
