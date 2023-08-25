/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata.test;

import org.sdase.commons.spring.boot.web.client.PlatformClient;
import org.springframework.web.bind.annotation.GetMapping;

@PlatformClient(name = "platformService", url = "${metadata.platformService.baseUrl}")
public interface MetadataPlatformServiceClient {
  @GetMapping("/metadata-platform-hello")
  Object getSomething();
}
