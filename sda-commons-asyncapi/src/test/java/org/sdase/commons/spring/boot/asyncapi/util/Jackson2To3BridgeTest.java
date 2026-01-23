/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.util;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class Jackson2To3BridgeTest {

  @Test
  void shouldReturnNullWhenInputIsNull() {
    Jackson2To3Bridge bridge = new Jackson2To3Bridge();

    tools.jackson.databind.JsonNode result = bridge.toJackson3(null);

    assertThat(result).isNull();
  }

  @Test
  void shouldConvertSimpleJsonObjectFromJackson2ToJackson3() throws Exception {
    Jackson2To3Bridge bridge = new Jackson2To3Bridge();

    com.fasterxml.jackson.databind.ObjectMapper jackson2 =
        new com.fasterxml.jackson.databind.ObjectMapper();

    com.fasterxml.jackson.databind.JsonNode jackson2Node =
        jackson2.readTree("{\"a\":1,\"b\":\"x\",\"ok\":true}");

    tools.jackson.databind.JsonNode jackson3Node = bridge.toJackson3(jackson2Node);

    assertThat(jackson3Node).isNotNull();
    assertThat(jackson3Node.isObject()).isTrue();
    assertThat(jackson3Node.get("a").asInt()).isEqualTo(1);
    assertThat(jackson3Node.get("b").asString()).isEqualTo("x");
    assertThat(jackson3Node.get("ok").asBoolean()).isTrue();
  }

  @Test
  void shouldPreserveNestedStructuresWhenConverting() throws Exception {
    Jackson2To3Bridge bridge = new Jackson2To3Bridge();

    com.fasterxml.jackson.databind.ObjectMapper jackson2 =
        new com.fasterxml.jackson.databind.ObjectMapper();

    com.fasterxml.jackson.databind.JsonNode jackson2Node =
        jackson2.readTree("{\"items\":[{\"id\":1},{\"id\":2}],\"meta\":{\"count\":2}}");

    tools.jackson.databind.JsonNode jackson3Node = bridge.toJackson3(jackson2Node);

    assertThat(jackson3Node.path("items")).hasSize(2);
    assertThat(jackson3Node.path("items").get(0).path("id").asInt()).isEqualTo(1);
    assertThat(jackson3Node.path("items").get(1).path("id").asInt()).isEqualTo(2);
    assertThat(jackson3Node.path("meta").path("count").asInt()).isEqualTo(2);
  }

  @Test
  void shouldWrapExceptionWhenJackson2SerializationFails() throws IOException {
    Jackson2To3Bridge bridge = new Jackson2To3Bridge();

    JsonNode jackson2Node = mock(JsonNode.class);

    doThrow(new RuntimeException("boom"))
        .when(jackson2Node)
        .serialize(any(JsonGenerator.class), any(SerializerProvider.class));

    assertThatThrownBy(() -> bridge.toJackson3(jackson2Node))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Failed to migrate JsonNode from Jackson 2 to 3")
        .hasCauseInstanceOf(JsonMappingException.class)
        .rootCause()
        .hasMessage("boom");

    verify(jackson2Node).serialize(any(JsonGenerator.class), any(SerializerProvider.class));
  }
}
