/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.jsonschema.victools;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.asyncapi.jsonschema.AbstractJsonSchemaBuilderTest;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.Plane;
import tools.jackson.databind.JsonNode;

class VictoolsJsonSchemaBuilderTest extends AbstractJsonSchemaBuilderTest {

  public VictoolsJsonSchemaBuilderTest() {
    super(VictoolsJsonSchemaBuilder.fromDefaultConfig());
  }

  @Override
  protected Set<DisabledSpec> disableSpecificFieldTests() {
    return Set.of();
  }

  @Test
  void shouldNotPrintGenericTypeInName() {
    Map<String, JsonNode> jsonSchema = jsonSchemaBuilder.toJsonSchema(Plane.class);
    assertThat(jsonSchema).containsKey("Plane").doesNotContainKey("Plane(Object)");
  }
}
