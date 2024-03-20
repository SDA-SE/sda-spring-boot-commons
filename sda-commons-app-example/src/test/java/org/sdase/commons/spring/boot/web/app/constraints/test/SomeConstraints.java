/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.constraints.test;

// ATTENTION: The source of this class is included in the public documentation.

import org.sdase.commons.spring.boot.web.auth.opa.AbstractConstraints;
import org.sdase.commons.spring.boot.web.auth.opa.Constraints;

@Constraints
public class SomeConstraints extends AbstractConstraints {
  private boolean admin;

  public boolean isAdmin() {
    return admin;
  }

  public SomeConstraints setAdmin(boolean admin) {
    this.admin = admin;
    return this;
  }
}
