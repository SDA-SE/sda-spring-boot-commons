/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
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
