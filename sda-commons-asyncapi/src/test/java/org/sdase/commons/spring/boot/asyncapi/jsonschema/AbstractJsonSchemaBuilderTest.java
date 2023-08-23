/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.jsonschema;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;
import static org.junit.jupiter.params.provider.Arguments.of;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.BaseEvent;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.CarScrapped;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Description.DescriptionSwaggerSchema;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Enums.EnumJackson;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Enums.EnumPlain;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Required.JakartaNotNull;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Required.JsonPropertyRequired;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Required.SwaggerRequired;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Required.SwaggerRequiredMode;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Temporal.TemporalDate;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Temporal.TemporalDuration;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Temporal.TemporalInstant;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Temporal.TemporalLocalDate;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Temporal.TemporalOffsetDateTime;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Temporal.TemporalPeriod;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Temporal.TemporalZonedDateTime;
import org.sdase.commons.spring.boot.web.testing.GoldenFileAssertions;

public abstract class AbstractJsonSchemaBuilderTest {

  protected static final String EXPECTED_SCHEMA_LOCATION =
      "src/test/resources/JsonSchemaBuilderTest";

  final JsonSchemaBuilder jsonSchemaBuilder;
  final String testClass;

  protected AbstractJsonSchemaBuilderTest(JsonSchemaBuilder jsonSchemaBuilder) {
    this.jsonSchemaBuilder = jsonSchemaBuilder;
    this.testClass = jsonSchemaBuilder.getClass().getSimpleName();
  }

  @MethodSource
  @ParameterizedTest
  void shouldProduceSchema(Class<?> givenSchemaBaseClass) throws IOException {
    Path schemaPath = buildExistingSchemaPath(givenSchemaBaseClass);
    var expected = jsonSchemaBuilder.toJsonSchema(givenSchemaBaseClass);
    String expectedYaml = new YAMLMapper().writeValueAsString(expected);
    GoldenFileAssertions.assertThat(schemaPath).hasYamlContentAndUpdateGolden(expectedYaml);
  }

  @MethodSource
  @ParameterizedTest
  void shouldSetSpecificFieldsOfDefinition(
      Class<?> givenBaseClass, String checkedPropertyPointer, JsonNode expectedValue) {

    assumeThat(disableSpecificFieldTests()).doesNotContain(givenBaseClass);

    var actualDefinitions = jsonSchemaBuilder.toJsonSchema(givenBaseClass);
    var actualSchema = schemaFromDefinitions(actualDefinitions, givenBaseClass);

    assertThat(actualSchema.at(checkedPropertyPointer)).isEqualTo(expectedValue);
  }

  protected JsonNode schemaFromDefinitions(Map<String, JsonNode> definitions, Class<?> baseClass) {
    assertThat(definitions).containsKey(baseClass.getSimpleName());
    return definitions.get(baseClass.getSimpleName());
  }

  private Path buildExistingSchemaPath(Class<?> givenSchemaBaseClass) throws IOException {
    var schemaPath =
        Path.of(
            "%s/schema_%s_%s_expected.yaml"
                .formatted(
                    EXPECTED_SCHEMA_LOCATION, givenSchemaBaseClass.getSimpleName(), testClass));
    if (!Files.exists(schemaPath)) {
      Files.write(schemaPath, new byte[] {});
    }
    return schemaPath;
  }

  static Stream<Arguments> shouldProduceSchema() {
    return Stream.of(of(BaseEvent.class), of(CarScrapped.class));
  }

  static Stream<Arguments> shouldSetSpecificFieldsOfDefinition() {
    return Stream.of(
        of(JsonPropertyRequired.class, "/required", array(text("requiredProperty"))),
        of(JakartaNotNull.class, "/required", array(text("requiredProperty"))),
        of(SwaggerRequired.class, "/required", array(text("requiredProperty"))),
        of(SwaggerRequiredMode.class, "/required", array(text("requiredProperty"))),
        of(TemporalDate.class, "/properties/dateTime/type", text("string")),
        of(TemporalDate.class, "/properties/dateTime/format", text("date-time")),
        of(TemporalZonedDateTime.class, "/properties/dateTime/type", text("string")),
        of(TemporalZonedDateTime.class, "/properties/dateTime/format", text("date-time")),
        of(TemporalOffsetDateTime.class, "/properties/dateTime/type", text("string")),
        of(TemporalOffsetDateTime.class, "/properties/dateTime/format", text("date-time")),
        of(TemporalInstant.class, "/properties/dateTime/type", text("string")),
        of(TemporalInstant.class, "/properties/dateTime/format", text("date-time")),
        of(TemporalLocalDate.class, "/properties/date/type", text("string")),
        of(TemporalLocalDate.class, "/properties/date/format", text("date")),
        of(TemporalDuration.class, "/properties/duration/type", text("string")),
        of(TemporalDuration.class, "/properties/duration/format", text("duration")),
        of(TemporalPeriod.class, "/properties/duration/type", text("string")),
        of(TemporalPeriod.class, "/properties/duration/format", text("duration")),
        of(DescriptionSwaggerSchema.class, "/properties/described/description", text("A property")),
        of(EnumPlain.class, "/type", text("string")),
        of(EnumPlain.class, "/enum", array(text("ONE"), text("TWO"))),
        of(EnumJackson.class, "/type", text("string")),
        of(EnumJackson.class, "/enum", array(text("1"), text("2")))
        // force linebreak at end
        );
  }

  static TextNode text(String text) {
    return new TextNode(text);
  }

  static ArrayNode array(JsonNode... items) {
    ObjectMapper om = new ObjectMapper();
    var arrayNode = om.createArrayNode();
    Stream.of(items).forEach(arrayNode::add);
    return arrayNode;
  }

  /**
   * Ideally, the implementation of this method returns an empty set. If not, it shows which
   * features are not supported by the implementation under test.
   *
   * @return all classes of tests defined in {@link #shouldSetSpecificFieldsOfDefinition()} that are
   *     not supported by the {@link JsonSchemaBuilder} implementation under test.
   */
  protected abstract Set<Class<?>> disableSpecificFieldTests();
}