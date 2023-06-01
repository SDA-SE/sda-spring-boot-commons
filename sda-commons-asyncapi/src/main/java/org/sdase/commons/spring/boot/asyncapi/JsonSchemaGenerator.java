/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/** Generator for JSON schemas from Jackson and mbknor-jackson-jsonSchema annotated Java classes. */
@Component
public class JsonSchemaGenerator {

  @Autowired private ApplicationContext applicationContext;

  private JsonSchemaGenerator() {
    // No public constructor
  }

  /**
   * Creates a new generator for JSON schemas
   *
   * @return builder
   */
  public JsonSchemaBuilder builder() {
    return applicationContext.getBean(JsonBuilder.class);
  }

  public interface JsonSchemaBuilder {

    /**
     * Includes a class into the schema.
     *
     * @param clazz The class to include
     * @param <T> The type of the class.
     * @return builder
     */
    <T> AdditionalPropertiesBuilder forClass(Class<T> clazz);
  }

  public interface AdditionalPropertiesBuilder extends FinalBuilder {

    /**
     * Whether the additionalProperties option in the generated schema is enabled.
     *
     * @param enabled If true, additionProperties is true.
     * @return builder
     */
    FinalBuilder allowAdditionalProperties(boolean enabled);
  }
}
