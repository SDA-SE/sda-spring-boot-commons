/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.auth.opa;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Service to access constraints returned by OPA authorization decisions. */
@Component
public class Constraints extends AbstractConstraints {

  public Constraints(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  /**
   * Gets the constraints as a generic map.
   *
   * @return the constraints map or null if not available
   */
  public Map<String, Object> getConstraintsAsMap() {
    return getConstraints(new TypeReference<Map<String, Object>>() {});
  }

  /**
   * Gets the constraints as a specific type.
   *
   * @param typeRef the type reference for the constraints
   * @param <T> the type of constraints
   * @return the constraints or null if not available
   */
  public <T> T getConstraints(TypeReference<T> typeRef) {
    return super.getConstraints(typeRef);
  }
}
