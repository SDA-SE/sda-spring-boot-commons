/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.jsonschema.victools;

import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.TypeScope;
import java.net.URI;

/** A module that adds the correct {@code format} to {@link URI} related types. */
public class UriFormatModule implements Module {

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.forTypesInGeneral().withStringFormatResolver(this::stringFormatResolver);
  }

  private String stringFormatResolver(TypeScope target) {
    var declaredType = target.getType();
    if (declaredType.isInstanceOf(URI.class)) {
      return "uri";
    }
    return null;
  }
}
