/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.tracing.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientTestController {

  OtherServiceClient otherServiceClient;

  public ClientTestController(OtherServiceClient otherServiceClient) {
    this.otherServiceClient = otherServiceClient;
  }

  @GetMapping("/proxy")
  public Object getSomethingFromOtherService() {
    return otherServiceClient.getSomething();
  }

  @GetMapping("/static")
  public String getStatic() {
    return "staticResponse";
  }
}
