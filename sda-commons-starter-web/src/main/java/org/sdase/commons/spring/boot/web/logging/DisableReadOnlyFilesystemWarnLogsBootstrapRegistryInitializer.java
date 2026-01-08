/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.logging;

import org.springframework.boot.bootstrap.BootstrapRegistry;
import org.springframework.boot.bootstrap.BootstrapRegistryInitializer;

/**
 * Disables warn logs that will raise if the filesystem is read only.
 *
 * @see <a href="https://sda-se.github.io/sda-spring-boot-commons/#static-directories">Docs</a>
 */
public class DisableReadOnlyFilesystemWarnLogsBootstrapRegistryInitializer
    implements BootstrapRegistryInitializer {
  @Override
  public void initialize(BootstrapRegistry registry) {
    System.setProperty("logging.level.org.apache.catalina.core", "error");
  }
}
