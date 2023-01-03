/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security;

import java.io.File;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextType;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementServerProperties;
import org.springframework.boot.actuate.autoconfigure.web.server.ManagementWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.autoconfigure.web.embedded.JettyWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.embedded.TomcatWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.embedded.UndertowWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.TomcatServletWebServerFactoryCustomizer;
import org.springframework.boot.autoconfigure.web.servlet.UndertowServletWebServerFactoryCustomizer;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/** FIXME: This is not affecting the management server yet. */
@Order(Ordered.HIGHEST_PRECEDENCE)
@ManagementContextConfiguration(value = ManagementContextType.CHILD, proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class ActuatorNoTempDirCreationConfiguration {

  @Bean
  ManagementWebServerFactoryCustomizer<ConfigurableServletWebServerFactory>
      servletManagementWebServerFactoryCustomizer(
          ListableBeanFactory beanFactory, @Value("${server.tomcat.basedir}") String baseDir) {
    return new NoTempDirCreationManagementWebServerFactoryCustomizer(
        beanFactory,
        baseDir,
        ServletWebServerFactoryCustomizer.class,
        TomcatServletWebServerFactoryCustomizer.class,
        TomcatWebServerFactoryCustomizer.class,
        JettyWebServerFactoryCustomizer.class,
        UndertowServletWebServerFactoryCustomizer.class,
        UndertowWebServerFactoryCustomizer.class);
  }

  static class NoTempDirCreationManagementWebServerFactoryCustomizer
      extends ManagementWebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {

    private final String baseDir;

    @SafeVarargs
    public NoTempDirCreationManagementWebServerFactoryCustomizer(
        ListableBeanFactory beanFactory,
        String baseDir,
        Class<? extends WebServerFactoryCustomizer<?>>... customizerClasses) {
      super(beanFactory, customizerClasses);
      this.baseDir = baseDir;
    }

    @Override
    public void customize(
        ConfigurableServletWebServerFactory factory,
        ManagementServerProperties managementServerProperties,
        ServerProperties serverProperties) {
      super.customize(factory, managementServerProperties, serverProperties);
      factory.setDocumentRoot(new File(baseDir).getParentFile());
    }
  }
}
