/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;

@AutoConfiguration
public class SdaMonitoringSecurityConfiguration {

  private static final Logger LOG =
      LoggerFactory.getLogger(SdaMonitoringSecurityConfiguration.class);

  private final int managementPort;

  public SdaMonitoringSecurityConfiguration(
      @Value("${management.server.port}") int managementPort) {
    this.managementPort = managementPort;
  }

  @Bean
  public WebSecurityCustomizer webSecurityCustomizer() {
    LOG.info("Enabling Monitoring on port '{}'", managementPort);
    return web ->
        web.ignoring().requestMatchers(request -> request.getLocalPort() == managementPort);
  }
}
