/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.test;

import java.util.ArrayList;
import java.util.List;

public class CreateSomethingResource {

  private List<String> items = new ArrayList<>();

  public List<String> getItems() {
    return items;
  }

  public CreateSomethingResource setItems(List<String> items) {
    this.items = items;
    return this;
  }
}
