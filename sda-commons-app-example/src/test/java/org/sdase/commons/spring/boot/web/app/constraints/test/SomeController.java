/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.constraints.test;

// ATTENTION: The source of this class is included in the public documentation.

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SomeController {

  private final SomeConstraints constraints;
  private final SomeService someService;

  public SomeController(SomeConstraints constraints, SomeService someService) {
    this.constraints = constraints;
    this.someService = someService;
  }

  @GetMapping("/me/category")
  public String getUserCategory() {
    return constraints.isAdmin() ? someService.doAsAdmin() : someService.doAsUser();
  }
}
