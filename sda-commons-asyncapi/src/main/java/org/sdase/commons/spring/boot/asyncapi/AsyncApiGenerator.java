/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi;

import com.fasterxml.jackson.databind.JsonNode;
import java.net.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Generator used to build AsyncAPI specs from a template base file and schemas generated from code.
 * The schemas are referenced via absolute $refs like
 *
 * <pre>
 *     payload:
 *       $ref: './partner-streaming-event.json#/definitions/CreateNaturalPersonEvent'
 * </pre>
 *
 * These referenced files are automatically created from Java classes annotated with Jackson and
 * mbknor-jackson-jsonSchema annotations. Afterwards everything is embedded into a single
 * self-contained file
 */
@Component
public class AsyncApiGenerator {

  @Autowired private ApplicationContext applicationContext;

  private AsyncApiGenerator() {
    // No public constructor
  }

  /**
   * Creates a new generator for AsyncAPI specs
   *
   * @return builder
   */
  public AsyncApiBaseBuilder builder() {
    return applicationContext.getBean(AsyncBuilder.class);
  }

  public interface AsyncApiBaseBuilder {

    /**
     * Supply a base AsyncAPI file to be used as a template
     *
     * @param url The resource url to the template file
     * @return builder
     */
    SchemaBuilder withAsyncApiBase(URL url);
  }

  public interface SchemaBuilder extends FinalBuilder {

    /**
     * Supply a JSON schema generate from clazz to the AsyncAPI.
     *
     * @param name The name of the JSON file under which the schema is referenced in the template
     *     file
     * @param clazz The class to generate a schema for
     * @param <T> The type of clazz
     * @return builder
     */
    <T> SchemaBuilder withSchema(String name, Class<T> clazz);

    /**
     * Supply a JSON schema from an existing JSON node
     *
     * @param name The name of the JSON file under which the schema is referenced in the template *
     *     file
     * @param node The node to embed
     * @return builder
     */
    SchemaBuilder withSchema(String name, JsonNode node);
  }
}
