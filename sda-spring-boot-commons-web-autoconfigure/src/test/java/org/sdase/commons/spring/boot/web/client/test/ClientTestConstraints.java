/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
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
