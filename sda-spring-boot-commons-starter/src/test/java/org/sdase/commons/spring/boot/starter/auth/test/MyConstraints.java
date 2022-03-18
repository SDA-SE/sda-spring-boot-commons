/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.auth.test;

import org.sdase.commons.spring.boot.starter.auth.opa.AbstractConstraints;
import org.sdase.commons.spring.boot.starter.auth.opa.Constraints;

@Constraints
public class MyConstraints extends AbstractConstraints {

  private boolean admin;

  public MyConstraints setAdmin(boolean admin) {
    this.admin = admin;
    return this;
  }

  public boolean isAdmin() {
    return admin;
  }
}
