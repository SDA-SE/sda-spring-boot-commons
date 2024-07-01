/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.docs;

import com.fasterxml.jackson.databind.SerializationFeature;
import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.ObjectMapperProvider;
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

  @Bean
  public ObjectMapperProvider objectMapperProvider() {
    SpringDocConfigProperties props = new SpringDocConfigProperties();
    var result = new ObjectMapperProvider(props);
    result.yamlMapper().enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    result.jsonMapper().enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
    return result;
  }
}
