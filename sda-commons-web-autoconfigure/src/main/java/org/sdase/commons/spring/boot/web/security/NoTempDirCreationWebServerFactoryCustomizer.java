/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security;

import java.io.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Component
public class NoTempDirCreationWebServerFactoryCustomizer
    implements WebServerFactoryCustomizer<TomcatServletWebServerFactory>, Ordered {

  @Value("${server.tomcat.basedir}")
  private String baseDir;

  @Override
  public void customize(TomcatServletWebServerFactory factory) {
    factory.setDocumentRoot(new File(baseDir).getParentFile());
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
