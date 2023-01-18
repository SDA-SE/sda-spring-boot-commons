/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.validation.trace;

import java.util.Arrays;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;

public class AllowTraceMethodConfig {
  @Bean
  public static HttpFirewall configureFirewall() {
    var strictHttpFirewall = new StrictHttpFirewall();
    // even allowed in the configured filterChain the `TRACE` method is rejected by the webserver
    strictHttpFirewall.setAllowedHttpMethods(
        Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "TRACE"));
    return strictHttpFirewall;
  }

  @Bean
  public static WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
    return customizer ->
        customizer.addConnectorCustomizers(connector -> connector.setAllowTrace(true));
  }
}
