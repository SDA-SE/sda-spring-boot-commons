/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.client.oidc;

import org.sdase.commons.spring.boot.starter.EnableSdaPlatform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@EnableSdaPlatform
@SpringBootApplication
@RestController
public class ClientCredentialsTestApp {

  @Autowired ClientCredentialsFlowFeignClient clientCredentialsFlowFeignClient;

  @GetMapping("/ping/external")
  public void ping() {
    clientCredentialsFlowFeignClient.pong();
  }
}
