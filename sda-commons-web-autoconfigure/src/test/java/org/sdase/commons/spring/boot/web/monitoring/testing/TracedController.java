/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring.testing;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TracedController {

  private final TracedFeignClient tracedFeignClient;

  public TracedController(TracedFeignClient tracedFeignClient) {
    this.tracedFeignClient = tracedFeignClient;
  }

  @GetMapping(path = "/pingMetrics")
  public String getPing() {
    return tracedFeignClient.pong();
  }
}
