/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A tree")
public class Tree {
  @Schema(description = "The name")
  private String name;

  public String getName() {
    return name;
  }

  public Tree setName(String name) {
    this.name = name;
    return this;
  }
}
