/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.tracing.test;

import org.sdase.commons.spring.boot.web.tracing.SdaTraceTokenClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
    name = "otherServiceClient",
    url = "${otherServiceClient.baseUrl}",
    configuration = {SdaTraceTokenClientConfiguration.class})
public interface OtherServiceClient {

  @GetMapping("/hello")
  Object getSomething();
}
