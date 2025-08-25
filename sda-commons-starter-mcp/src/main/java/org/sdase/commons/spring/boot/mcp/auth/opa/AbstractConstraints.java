/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.auth.opa;

import static org.springframework.web.context.request.RequestAttributes.SCOPE_REQUEST;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sdase.commons.spring.boot.mcp.auth.opa.model.OpaResponse;
import org.springframework.web.context.request.RequestContextHolder;

/** Abstract base class for constraints returned by OPA decisions. */
public abstract class AbstractConstraints {

  protected final ObjectMapper objectMapper;

  protected AbstractConstraints(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  /**
   * Gets the constraints from the current request context.
   *
   * @param typeRef the type reference for the constraints
   * @param <T> the type of constraints
   * @return the constraints or null if not available
   */
  protected <T> T getConstraints(TypeReference<T> typeRef) {
    try {
      var requestAttributes = RequestContextHolder.currentRequestAttributes();
      var opaResponse =
          (OpaResponse)
              requestAttributes.getAttribute(
                  OpaAuthorizationManager.CONSTRAINTS_ATTRIBUTE, SCOPE_REQUEST);
      if (opaResponse != null && opaResponse.getResult() != null) {
        return objectMapper.convertValue(opaResponse.getResult(), typeRef);
      }
    } catch (IllegalStateException | NullPointerException ignored) {
      // No request context available or no constraints
    }
    return null;
  }
}
