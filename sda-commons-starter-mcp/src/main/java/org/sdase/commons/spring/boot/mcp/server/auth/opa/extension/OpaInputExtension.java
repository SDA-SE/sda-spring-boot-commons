/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth.opa.extension;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Extension that provides additional content to be sent to OPA for authorization decisions.
 *
 * @param <T> the type of the additional input content
 */
public interface OpaInputExtension<T> {

  /**
   * @return the namespace to use for the additional content in the OPA input
   */
  String getNamespace();

  /**
   * Creates additional content to be sent to OPA for authorization decisions.
   *
   * @param httpServletRequest the current request
   * @return the additional content
   */
  T createAdditionalInputContent(HttpServletRequest httpServletRequest);
}
