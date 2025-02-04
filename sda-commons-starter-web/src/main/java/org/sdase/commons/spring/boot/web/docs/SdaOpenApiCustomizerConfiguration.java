/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.docs;

import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource("classpath:/org/sdase/commons/spring/boot/web/docs/defaults.properties")
public class SdaOpenApiCustomizerConfiguration {

  @Bean
  public ServerBaseUrlCustomizer removeServerBaseUrl() {
    return (serverBaseUrl, httpRequest) -> null;
  }
}
