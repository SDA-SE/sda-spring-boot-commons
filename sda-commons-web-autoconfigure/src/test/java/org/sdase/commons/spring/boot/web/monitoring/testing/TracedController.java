/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
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
