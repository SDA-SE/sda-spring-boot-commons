/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.test;

import jakarta.validation.constraints.NotNull;

public class TestResource {
  @NotNull(message = "must not be null")
  private String value;

  @NumericString(message = "postCode should be numeric")
  private String postcode;

  public String getValue() {
    return value;
  }

  public TestResource setValue(String value) {
    this.value = value;
    return this;
  }

  public String getPostcode() {
    return postcode;
  }

  public TestResource setPostcode(String postcode) {
    this.postcode = postcode;
    return this;
  }
}
