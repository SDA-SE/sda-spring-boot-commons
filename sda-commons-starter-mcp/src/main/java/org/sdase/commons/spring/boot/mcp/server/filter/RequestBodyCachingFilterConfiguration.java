/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.filter;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

/**
 * Auto-configuration for the RequestBodyCachingFilter. This configuration ensures that the
 * RequestBodyCachingFilter is properly registered in the Spring Boot application context with the
 * correct order precedence.
 */
@AutoConfiguration
public class RequestBodyCachingFilterConfiguration {

  /**
   * Registers the RequestBodyCachingFilter with the highest precedence to ensure it runs before
   * other filters that might need to access the request body multiple times.
   *
   * @return A FilterRegistrationBean for the RequestBodyCachingFilter
   */
  @Bean
  public FilterRegistrationBean<RequestBodyCachingFilter> requestBodyCachingFilter() {
    RequestBodyCachingFilter filter = new RequestBodyCachingFilter();
    FilterRegistrationBean<RequestBodyCachingFilter> registration =
        new FilterRegistrationBean<>(filter);
    // Set the filter to run with highest precedence, ensuring it's processed before other filters
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }
}
