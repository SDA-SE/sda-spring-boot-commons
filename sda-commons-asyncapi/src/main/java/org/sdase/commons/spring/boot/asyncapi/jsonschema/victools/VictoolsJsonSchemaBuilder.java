/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.jsonschema.victools;

import static com.github.victools.jsonschema.generator.Option.ALLOF_CLEANUP_AT_THE_END;
import static com.github.victools.jsonschema.generator.Option.DEFINITIONS_FOR_ALL_OBJECTS;
import static com.github.victools.jsonschema.generator.Option.DEFINITIONS_FOR_MEMBER_SUPERTYPES;
import static com.github.victools.jsonschema.generator.Option.DEFINITION_FOR_MAIN_SCHEMA;
import static com.github.victools.jsonschema.module.jackson.JacksonOption.FLATTENED_ENUMS_FROM_JSONPROPERTY;
import static com.github.victools.jsonschema.module.jackson.JacksonOption.INLINE_TRANSFORMED_SUBTYPES;
import static com.github.victools.jsonschema.module.jackson.JacksonOption.RESPECT_JSONPROPERTY_ORDER;
import static com.github.victools.jsonschema.module.jackson.JacksonOption.RESPECT_JSONPROPERTY_REQUIRED;
import static com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationOption.INCLUDE_PATTERN_EXPRESSIONS;
import static com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationOption.NOT_NULLABLE_FIELD_IS_REQUIRED;

import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfig;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import com.github.victools.jsonschema.module.jackson.JacksonSchemaModule;
import com.github.victools.jsonschema.module.jakarta.validation.JakartaValidationModule;
import com.github.victools.jsonschema.module.swagger2.Swagger2Module;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import org.sdase.commons.spring.boot.asyncapi.jsonschema.JsonSchemaBuilder;
import org.sdase.commons.spring.boot.asyncapi.util.RefUtil;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * A {@link JsonSchemaBuilder} that uses <a
 * href="https://github.com/victools/jsonschema-generator">victools/json-schema-generator</a> to
 * build Json Schemas from Java code.
 */
public class VictoolsJsonSchemaBuilder implements JsonSchemaBuilder {

  private final SchemaGenerator schemaGenerator;

  /**
   * @return a {@link JsonSchemaBuilder} generating schemas suitable for AsyncAPI.
   */
  public static VictoolsJsonSchemaBuilder fromDefaultConfig() {
    var jacksonModule =
        new JacksonSchemaModule(
            RESPECT_JSONPROPERTY_ORDER,
            RESPECT_JSONPROPERTY_REQUIRED,
            INLINE_TRANSFORMED_SUBTYPES,
            FLATTENED_ENUMS_FROM_JSONPROPERTY);
    var jakartaValidationModule =
        new JakartaValidationModule(INCLUDE_PATTERN_EXPRESSIONS, NOT_NULLABLE_FIELD_IS_REQUIRED);
    var swagger2Module = new Swagger2Module();
    SchemaGeneratorConfigBuilder configBuilder =
        new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON)
            .with(DEFINITIONS_FOR_ALL_OBJECTS)
            .with(DEFINITION_FOR_MAIN_SCHEMA)
            .with(DEFINITIONS_FOR_MEMBER_SUPERTYPES)
            .with(ALLOF_CLEANUP_AT_THE_END)
            .with(jacksonModule)
            .with(jakartaValidationModule)
            .with(swagger2Module)
            .with(new SwaggerExampleModule())
            .with(new NotBlankModule());
    // see https://github.com/victools/jsonschema-generator/issues/125#issuecomment-657014858
    configBuilder
        .forTypesInGeneral()
        .withPropertySorter((o1, o2) -> 0)
        .withDefinitionNamingStrategy(new DefaultSchemaDefinitionNamingStrategy());

    SchemaGeneratorConfig config = configBuilder.build();
    return new VictoolsJsonSchemaBuilder(config);
  }

  /**
   * A {@link VictoolsJsonSchemaBuilder} with custom configuration.
   *
   * @param schemaGeneratorConfig the configuration
   */
  public VictoolsJsonSchemaBuilder(SchemaGeneratorConfig schemaGeneratorConfig) {
    this.schemaGenerator = new SchemaGenerator(schemaGeneratorConfig);
  }

  @Override
  public Map<String, JsonNode> toJsonSchema(Type type) {
    var schema = toSchemaMap(schemaGenerator.generateSchema(type));
    schema.values().forEach(this::updateRefsInPlace);
    return schema;
  }

  private void updateRefsInPlace(JsonNode jsonSchemaOfType) {
    RefUtil.updateAllRefsRecursively(
        jsonSchemaOfType, this::convertRefToDefinitionsIntoRefToComponentsSchemas);
  }

  private String convertRefToDefinitionsIntoRefToComponentsSchemas(StringNode refValue) {
    String ref = refValue.asString();
    if (ref.startsWith("#/definitions/")) {
      return "#/components/schemas/" + ref.substring("#/definitions/".length());
    } else {
      return ref;
    }
  }

  private Map<String, JsonNode> toSchemaMap(ObjectNode jsonNodesFromVictools) {
    JsonNode generatedDefinitions = jsonNodesFromVictools.get("definitions");
    Map<String, JsonNode> definitions = new LinkedHashMap<>();
    generatedDefinitions
        .propertyNames()
        .forEach(name -> definitions.put(name, generatedDefinitions.get(name)));
    return definitions;
  }
}
