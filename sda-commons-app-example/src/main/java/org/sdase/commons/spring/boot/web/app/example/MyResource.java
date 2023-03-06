/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A resource")
public class MyResource {
  @Schema(description = "A value")
  private String value;

  public String getValue() {
    return value;
  }

  public MyResource setValue(String value) {
    this.value = value;
    return this;
  }
}
