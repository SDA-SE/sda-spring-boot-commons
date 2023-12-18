/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.platform.test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientTestController {

  private final PlatformClientService platformClientService;

  private final ExternalClientService externalClientService;

  public ClientTestController(
      PlatformClientService platformClientService, ExternalClientService externalClientService) {
    this.platformClientService = platformClientService;
    this.externalClientService = externalClientService;
  }

  @GetMapping("/proxy")
  public Object getSomethingFromOtherService() {
    return platformClientService.getSomething();
  }

  @GetMapping("/proxy-external")
  public Object getSomethingFromExternalService() {
    return externalClientService.getSomething();
  }

  @GetMapping("/static")
  public String getStatic() {
    return "staticResponse";
  }
}
