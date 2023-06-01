/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.asyncapi.models.BaseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = AsyncTestApp.class)
class JsonSchemaGeneratorTest {

  @Autowired private JsonSchemaGenerator jsonSchemaGenerator;

  @Test
  void shouldGenerateJsonSchema() throws IOException, URISyntaxException {
    String actual = jsonSchemaGenerator.builder().forClass(BaseEvent.class).generateYaml();
    String expected = TestUtil.readResource("/schema_expected.yaml");

    Map<String, Object> expectedJson =
        YAMLMapper.builder()
            .build()
            .readValue(expected, new TypeReference<Map<String, Object>>() {});
    Map<String, Object> actualJson =
        YAMLMapper.builder().build().readValue(actual, new TypeReference<Map<String, Object>>() {});

    assertThat(actualJson).usingRecursiveComparison().isEqualTo(expectedJson);
  }

  @Test
  void shouldSortDefinitions() {
    JsonNode actual = jsonSchemaGenerator.builder().forClass(BaseEvent.class).generate();
    JsonNode definitions = actual.at("/definitions");
    List<String> keys = new ArrayList<>();
    definitions.fieldNames().forEachRemaining(keys::add);
    // usingRecursiveComparison() is unable to compare the order, so we have to do it manually.
    assertThat(keys).isSorted();
  }
}
