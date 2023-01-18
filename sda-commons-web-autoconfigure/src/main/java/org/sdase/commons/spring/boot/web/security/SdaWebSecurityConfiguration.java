/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security;

import org.sdase.commons.spring.boot.web.security.headers.RestfulApiSecurityConfiguration;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.web.filter.ForwardedHeaderFilter;

@EnableWebSecurity
@ComponentScan
@AutoConfiguration
@Import({RestfulApiSecurityConfiguration.class, SdaCorsConfigurer.class})
public class SdaWebSecurityConfiguration {
  @Bean
  public FilterRegistrationBean<ForwardedHeaderFilter> forwardedHeaderFilter() {
    ForwardedHeaderFilter filter = new ForwardedHeaderFilter();
    FilterRegistrationBean<ForwardedHeaderFilter> registration =
        new FilterRegistrationBean<>(filter);
    registration.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return registration;
  }
}
