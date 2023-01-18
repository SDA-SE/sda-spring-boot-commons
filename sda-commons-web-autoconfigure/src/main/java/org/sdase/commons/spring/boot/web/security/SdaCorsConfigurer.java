/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Component
@ConfigurationProperties("cors")
public class SdaCorsConfigurer implements WebMvcConfigurer {

  /**
   * Comma-separated list of origin patterns to allow. Unlike allowed origins which only supports
   * '*', origin patterns are more flexible (for example 'https://*.example.com') and can be used
   * when credentials are allowed. When no allowed origin patterns or allowed origins are set, CORS
   * support is disabled.
   */
  private List<String> allowedOriginPatterns = new ArrayList<>();

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    CorsRegistration corsRegistration = registry.addMapping("/**");
    if (allowedOriginPatterns.isEmpty()) {
      corsRegistration.allowedOrigins(); // effectively disallows cors
    } else {
      corsRegistration.allowedOriginPatterns(allowedOriginPatterns.toArray(new String[] {}));
    }
  }

  public void setAllowedOriginPatterns(List<String> allowedOriginPatterns) {
    this.allowedOriginPatterns = allowedOriginPatterns;
  }
}
