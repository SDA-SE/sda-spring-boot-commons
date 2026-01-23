/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.dataformat.yaml.YAMLMapper;

class JsonNodeUtilTest {

  @Test
  void shouldSortJsonNodeInPlace() {

    String sortString = "{\"toSort\": {\"b\": 1, \"z\": 2, \"a\": 0}}";
    JsonNode jsonNode = YAMLMapper.builder().build().readTree(sortString);
    JsonNode nodeToSort = jsonNode.at("/toSort");
    JsonNodeUtil.sortJsonNodeInPlace(nodeToSort);
    List<String> keys = new ArrayList<>(nodeToSort.propertyNames());
    assertThat(keys).isSorted();
  }
}
