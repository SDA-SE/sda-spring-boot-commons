/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.logging;

import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.BootstrapRegistryInitializer;

/**
 * Disables the Spring Boot Banner because it will never be printed in JSON logging format.
 * Unfortunately, this setting is not picked up from the various `default.properties` provided by
 * sda-spring-boot-commons. Components and their {@link
 * org.springframework.context.annotation.PropertySource} annotations are scanned after the banner
 * is printed.
 */
public class DisableBannerBootstrapRegistryInitializer implements BootstrapRegistryInitializer {
  @Override
  public void initialize(BootstrapRegistry registry) {
    System.setProperty("spring.main.banner-mode", "off");
  }
}
