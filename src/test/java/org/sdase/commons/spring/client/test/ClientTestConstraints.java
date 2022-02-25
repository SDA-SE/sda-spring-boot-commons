/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.client.test;

import org.sdase.commons.spring.auth.opa.AbstractConstraints;
import org.sdase.commons.spring.auth.opa.Constraints;

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
