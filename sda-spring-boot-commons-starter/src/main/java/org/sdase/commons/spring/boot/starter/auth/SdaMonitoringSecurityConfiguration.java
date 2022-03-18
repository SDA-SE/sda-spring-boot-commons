/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@ConditionalOnClass(name = "org.sdase.commons.spring.monitoring.SdaMonitoringConfiguration")
public class SdaMonitoringSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private static final Logger LOG =
      LoggerFactory.getLogger(SdaMonitoringSecurityConfiguration.class);

  private final int managementPort;

  public SdaMonitoringSecurityConfiguration(
      @Value("${management.server.port}") int managementPort) {
    this.managementPort = managementPort;
  }

  @Override
  public void configure(WebSecurity web) {
    LOG.info("Enabling Monitoring on port '{}'", managementPort);
    web.ignoring().requestMatchers(request -> request.getLocalPort() == managementPort);
  }
}
