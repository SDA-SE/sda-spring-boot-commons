/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.SortedMap;
import java.util.TreeMap;

public class JsonNodeUtil {

  private JsonNodeUtil() {
    // No constructor
  }

  public static void sortJsonNodeInPlace(JsonNode node) {
    if (!node.isMissingNode() && node.isObject()) {
      ObjectNode objectNode = (ObjectNode) node;
      SortedMap<String, JsonNode> fields = new TreeMap<>();
      objectNode.fields().forEachRemaining(e -> fields.put(e.getKey(), e.getValue()));
      objectNode.removeAll();
      fields.forEach(objectNode::set);
    }
  }
}
