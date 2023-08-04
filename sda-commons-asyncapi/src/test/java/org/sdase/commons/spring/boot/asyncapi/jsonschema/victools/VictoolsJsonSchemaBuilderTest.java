/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.jsonschema.victools;

import java.util.Set;
import org.sdase.commons.spring.boot.asyncapi.jsonschema.AbstractJsonSchemaBuilderTest;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Required.SwaggerRequiredMode;

class VictoolsJsonSchemaBuilderTest extends AbstractJsonSchemaBuilderTest {

  public VictoolsJsonSchemaBuilderTest() {
    super(VictoolsJsonSchemaBuilder.fromDefaultConfig());
  }

  @Override
  protected Set<Class<?>> disableSpecificFieldTests() {
    return Set.of(
        // Schema.requiredMode() not supported, but Schema.required() is
        SwaggerRequiredMode.class);
  }
}
