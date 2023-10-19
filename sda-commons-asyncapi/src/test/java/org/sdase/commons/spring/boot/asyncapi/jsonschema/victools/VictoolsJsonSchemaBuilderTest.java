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
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Required.JakartaNotBlank;
import org.sdase.commons.spring.boot.asyncapi.test.data.models.MinimalTestModels.Required.SwaggerRequiredMode;

class VictoolsJsonSchemaBuilderTest extends AbstractJsonSchemaBuilderTest {

  public VictoolsJsonSchemaBuilderTest() {
    super(VictoolsJsonSchemaBuilder.fromDefaultConfig());
  }

  @Override
  protected Set<DisabledSpec> disableSpecificFieldTests() {
    return Set.of(
        // Schema.requiredMode() not supported, but Schema.required() is
        disable(SwaggerRequiredMode.class, "/required"),
        // pattern not set, schema would allow " ", but validation does not
        // work around: @Pattern(regexp = "^.*\\S+.*$") @NotBlank
        disable(JakartaNotBlank.class, "/properties/notBlankProperty/pattern"));
  }
}
