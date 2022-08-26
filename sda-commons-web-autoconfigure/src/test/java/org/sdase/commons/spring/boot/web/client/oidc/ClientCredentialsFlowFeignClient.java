/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client.oidc;

import org.sdase.commons.spring.boot.web.client.OidcClientRequestConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
    value = "clientCredentialsFeignClient",
    url = "${feign.test.api.base.url}",
    configuration = OidcClientRequestConfiguration.class)
public interface ClientCredentialsFlowFeignClient {

  @GetMapping("/pong")
  ResponseEntity<Void> pong();
}
