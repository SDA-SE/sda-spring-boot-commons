/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client.test;

import org.sdase.commons.spring.boot.web.auth.opa.AbstractConstraints;
import org.sdase.commons.spring.boot.web.auth.opa.Constraints;

@Constraints
public class ClientTestConstraints extends AbstractConstraints {

  private boolean callAsyncAllowed;

  public boolean isCallAsyncAllowed() {
    return callAsyncAllowed;
  }

  public ClientTestConstraints setCallAsyncAllowed(boolean callAsyncAllowed) {
    this.callAsyncAllowed = callAsyncAllowed;
    return this;
  }
}
