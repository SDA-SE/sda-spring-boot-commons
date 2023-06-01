/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.kjetland.jackson.jsonSchema.JsonSchemaConfig;
import com.kjetland.jackson.jsonSchema.JsonSchemaDraft;
import org.sdase.commons.spring.boot.asyncapi.internal.JsonNodeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonBuilder
    implements JsonSchemaGenerator.JsonSchemaBuilder,
        JsonSchemaGenerator.AdditionalPropertiesBuilder,
        FinalBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(JsonBuilder.class);

  @Autowired private ObjectMapper objectMapper;

  boolean allowAdditionalPropertiesEnabled = false;
  Class<?> clazz;

  @Override
  public <T> JsonSchemaGenerator.AdditionalPropertiesBuilder forClass(Class<T> clazz) {
    this.clazz = clazz;
    return this;
  }

  @Override
  public JsonNode generate() {

    com.kjetland.jackson.jsonSchema.JsonSchemaGenerator jsonSchemaGenerator =
        new com.kjetland.jackson.jsonSchema.JsonSchemaGenerator(
            objectMapper,
            JsonSchemaConfig.vanillaJsonSchemaDraft4()
                // We use JSON schema draft 07 here explicitly and not the latest version, as
                // AsyncAPI uses DRAFT 07:
                // https://www.asyncapi.com/docs/specifications/2.0.0/#a-name-messageobjectschemaformattable-a-schema-formats-table
                .withJsonSchemaDraft(JsonSchemaDraft.DRAFT_07)
                .withFailOnUnknownProperties(!allowAdditionalPropertiesEnabled));
    JsonNode jsonNode = jsonSchemaGenerator.generateJsonSchema(clazz);
    JsonNodeUtil.sortJsonNodeInPlace(jsonNode.at("/definitions"));
    return jsonNode;
  }

  @Override
  public String generateYaml() {
    try {

      return YAMLMapper.builder().build().writeValueAsString(generate());
    } catch (JsonProcessingException e) {

      LOG.error("Error while converting JSON to YAML: ", e);
    }

    return null;
  }

  @Override
  public FinalBuilder allowAdditionalProperties(boolean enabled) {
    this.allowAdditionalPropertiesEnabled = enabled;
    return this;
  }
}
