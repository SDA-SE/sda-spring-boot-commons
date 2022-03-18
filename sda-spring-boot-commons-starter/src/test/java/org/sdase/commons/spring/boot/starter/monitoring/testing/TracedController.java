/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.monitoring.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TracedController {

  private static final Logger LOGGER = LoggerFactory.getLogger(TracedController.class);

  private final TracedFeignClient tracedFeignClient;

  public TracedController(TracedFeignClient tracedFeignClient) {
    this.tracedFeignClient = tracedFeignClient;
  }

  @GetMapping()
  public String ping() {
    return tracedFeignClient.pong();
  }
}
