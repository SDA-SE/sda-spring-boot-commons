/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.validation;

import org.apache.catalina.connector.Connector;
import org.sdase.commons.spring.boot.web.security.exception.InsecureConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.boot.web.server.WebServer;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Checks that secure defaults of used {@link Connector} instances are not modified and overwrites
 * insecure defaults. This class checks for the risks identified in the security guide as:
 *
 * <ul>
 *   <li>"Risk: Exploitation of HTTP-Methods"
 * </ul>
 */
@Component
public class HttpMethodsSecurityAdvice implements ApplicationListener<WebServerInitializedEvent> {
  private static final Logger LOG = LoggerFactory.getLogger(HttpMethodsSecurityAdvice.class);

  @Override
  public void onApplicationEvent(WebServerInitializedEvent event) {
    assertWebServerDoesNotAllowTrace(event.getWebServer());
  }

  private static void assertWebServerDoesNotAllowTrace(WebServer webServer) {
    if (webServer instanceof TomcatWebServer tomcatWebServer) {
      Connector[] connectors = tomcatWebServer.getTomcat().getService().findConnectors();
      for (Connector connector : connectors) {
        HttpMethodsSecurityAdvice.assertTomcatDoesNotAllowTrace(connector);
      }
    } else {
      // may add more checks for other webservers (jetty, undertow...)
      LOG.warn(
          "Security for web server of type {} is not supported yet.",
          webServer.getClass().getSimpleName());
    }
  }

  private static void assertTomcatDoesNotAllowTrace(Connector connector) {
    // Prevent the application from starting
    if (connector.getAllowTrace()) {
      throw new InsecureConfigurationException("The server accepts insecure methods.");
    }
  }
}
