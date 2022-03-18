/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.client.test;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "other", url = "${other.baseUrl}")
public interface OtherServiceClient {

  @GetMapping("/hello")
  Object getSomething();
}
