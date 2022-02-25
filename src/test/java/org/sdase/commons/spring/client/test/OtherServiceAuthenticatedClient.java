/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.client.test;

import org.sdase.commons.spring.client.AuthenticationPassThroughClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
    value = "otherAuthenticated",
    url = "${otherAuthenticated.baseUrl}",
    configuration = {AuthenticationPassThroughClientConfiguration.class})
public interface OtherServiceAuthenticatedClient {

  @GetMapping("/hello")
  Object getSomething();
}
