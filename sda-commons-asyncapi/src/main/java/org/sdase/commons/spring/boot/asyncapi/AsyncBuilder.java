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
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import org.sdase.commons.spring.boot.asyncapi.internal.JsonNodeUtil;
import org.sdase.commons.spring.boot.asyncapi.internal.JsonSchemaEmbedder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class AsyncBuilder
    implements AsyncApiGenerator.AsyncApiBaseBuilder, AsyncApiGenerator.SchemaBuilder {

  private static final Logger LOG = LoggerFactory.getLogger(AsyncBuilder.class);

  @Autowired private JsonSchemaGenerator jsonSchemaGenerator;

  private JsonNode asyncApiBaseTemplate;
  private final Map<String, JsonNode> schemas = new HashMap<>();
  private final JsonSchemaEmbedder jsonSchemaEmbedder =
      new JsonSchemaEmbedder(
          "/components/schemas",
          key -> {
            if (!schemas.containsKey(key)) {
              throw new UnknownSchemaException("Can't find schema for URL '" + key + "'");
            }
            return schemas.get(key);
          });

  @Override
  public AsyncApiGenerator.SchemaBuilder withAsyncApiBase(URL url) {
    try {

      asyncApiBaseTemplate = YAMLMapper.builder().build().readTree(url);
    } catch (IOException e) {

      LOG.error("ERROR while converting YAML to JSONNode", e);
    }
    return this;
  }

  @Override
  public <T> AsyncApiGenerator.SchemaBuilder withSchema(String name, Class<T> clazz) {
    schemas.put(
        name,
        jsonSchemaGenerator.builder().forClass(clazz).allowAdditionalProperties(true).generate());

    return this;
  }

  @Override
  public AsyncApiGenerator.SchemaBuilder withSchema(String name, JsonNode node) {
    schemas.put(name, node);

    return this;
  }

  @Override
  public JsonNode generate() {
    JsonNode jsonNode = jsonSchemaEmbedder.resolve(asyncApiBaseTemplate);
    JsonNodeUtil.sortJsonNodeInPlace(jsonNode.at("/components/schemas"));
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
}
