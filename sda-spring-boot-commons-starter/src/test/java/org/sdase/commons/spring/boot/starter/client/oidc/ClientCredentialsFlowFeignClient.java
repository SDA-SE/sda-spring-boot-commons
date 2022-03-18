/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.client.oidc;

import org.sdase.commons.spring.boot.starter.client.OidcClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
    value = "clientCredentialsFeignClient",
    url = "${feign.test.api.base.url}",
    configuration = OidcClientConfiguration.class)
public interface ClientCredentialsFlowFeignClient {

  @GetMapping("/pong")
  ResponseEntity<Void> pong();
}
