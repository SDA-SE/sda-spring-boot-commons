/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.client;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@PropertySource("classpath:/org/sdase/commons/spring/boot/web/client/default.properties")
public class SdaClientConfiguration implements WebMvcConfigurer {

  @Bean
  public AuthorizationStoreRequestInterceptor authorizationStoreRequestInterceptor() {
    return new AuthorizationStoreRequestInterceptor();
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authorizationStoreRequestInterceptor());
  }
}
