/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client.test;

import org.sdase.commons.spring.boot.web.client.AuthenticationPassThroughClientConfiguration;
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
