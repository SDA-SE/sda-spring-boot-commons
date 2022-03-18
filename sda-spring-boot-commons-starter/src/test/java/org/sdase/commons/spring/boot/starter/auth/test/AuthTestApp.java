/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.auth.test;

import java.security.Principal;
import java.util.Map;
import org.sdase.commons.spring.boot.starter.EnableSdaPlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableSdaPlatform
@RestController
public class AuthTestApp {

  private static final Logger LOG = LoggerFactory.getLogger(AuthTestApp.class);

  @Autowired private MyConstraints myConstraints;

  @GetMapping("/ping")
  public Object getPing(Principal principal) {
    if (principal == null) {
      LOG.info("Principal is null");
    } else {
      LOG.info("Principal is {}", principal.getName());
    }
    return Map.of(
        "ping",
        "pong",
        "principal",
        principal == null ? "null" : principal.getName(),
        "authenticated",
        principal != null,
        "admin",
        myConstraints.isAdmin());
  }

  @PostMapping("/ping")
  public Object postPing(Principal principal) {
    if (principal == null) {
      LOG.info("Principal is null");
    } else {
      LOG.info("Principal is {}", principal.getName());
    }
    return Map.of(
        "ping",
        "pong",
        "principal",
        principal == null ? "null" : principal.getName(),
        "authenticated",
        principal != null);
  }

  @GetMapping("/ping/{pingParam}")
  public Object getSpecificPing(Principal principal, @PathVariable String pingParam) {
    if (principal == null) {
      LOG.info("Principal is null");
    } else {
      LOG.info("Principal is {}", principal.getName());
    }
    return Map.of(
        "ping",
        "pong",
        "param",
        pingParam,
        "principal",
        principal == null ? "null" : principal.getName(),
        "authenticated",
        principal != null);
  }
}
