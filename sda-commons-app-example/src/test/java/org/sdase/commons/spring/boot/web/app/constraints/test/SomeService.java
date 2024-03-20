/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.constraints.test;

import org.springframework.stereotype.Component;

@Component
public class SomeService {
  public String doAsAdmin() {
    return "admin";
  }

  public String doAsUser() {
    return "user";
  }
}
