/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.exception;

import tools.jackson.core.JacksonException;

/** Wraps {@link JacksonException} as a new RuntimeException, to allow message changes. */
public class JacksonYamlException extends RuntimeException {

  public JacksonYamlException(String message, JacksonException cause) {
    super(message, cause);
  }

  public JacksonYamlException(JacksonException cause) {
    super(cause);
  }
}
