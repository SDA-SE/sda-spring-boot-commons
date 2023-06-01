/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Test;

class JsonSchemaEmbedderTest {

  @Test
  void shouldEmbedObject() throws IOException {

    URL resource = getClass().getResource("/json_schema_embedder/simple_object_input.yaml");
    JsonNode input = YAMLMapper.builder().build().readTree(resource);

    JsonSchemaEmbedder embedder =
        new JsonSchemaEmbedder(
            "/definitions",
            name -> {
              URL resourceUrl = getClass().getResource("/json_schema_embedder/" + name);
              try {

                return YAMLMapper.builder().build().readTree(resourceUrl);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });
    JsonNode result = embedder.resolve(input);

    assertThat(result.at("/definitions").fieldNames())
        .toIterable()
        .containsExactlyInAnyOrder(
            "Person", "Address", "Country", "simpleobjectsimplereferencedyaml");
    assertThat(result.at("/definitions/Person/properties/address/$ref").asText())
        .isEqualTo("#/definitions/Address");
    assertThat(result.at("/definitions/Address/properties/country/$ref").asText())
        .isEqualTo("#/definitions/Country");
    assertThat(result.at("/definitions/simpleobjectsimplereferencedyaml/$schema").isMissingNode())
        .isTrue();
  }

  @Test
  void shouldNotEmbedObjectsAsURLs() throws IOException {
    URL resource = getClass().getResource("/json_schema_embedder/simple_object_url.yaml");
    JsonNode input = YAMLMapper.builder().build().readTree(resource);

    JsonSchemaEmbedder embedder =
        new JsonSchemaEmbedder(
            "/definitions",
            name -> {
              URL resourceUrl = getClass().getResource("/json_schema_embedder/" + name);
              try {

                return YAMLMapper.builder().build().readTree(resourceUrl);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            });

    JsonNode result = embedder.resolve(input);

    assertThat(result.at("/definitions").fieldNames())
        .toIterable()
        .containsExactlyInAnyOrder("Person", "simpleobjectsimplereferencedyaml");
    assertThat(result.at("/definitions/simpleobjectsimplereferencedyaml").isMissingNode())
        .isFalse();
    assertThat(result.at("/definitions/address").isMissingNode()).isTrue();
    assertThat(result.at("/definitions/Person/properties/address/$ref").asText())
        .isEqualTo("https://sda.se");
  }
}
