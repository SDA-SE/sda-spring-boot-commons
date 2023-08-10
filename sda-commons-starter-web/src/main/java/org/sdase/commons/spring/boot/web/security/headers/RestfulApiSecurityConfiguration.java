/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.headers;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * This filter adds headers to the response that enhance the security of applications that serve
 * only REST APIs. The following risks are addressed:
 *
 * <ul>
 *   <li>"Risk: Cross Site Scripting (XSS)"
 *   <li>"Risk: Reloading content into Flash and PDFs"
 *   <li>"Risk: Clickjacking"
 *   <li>"Risk: Passing on visited URLs to third parties"
 *   <li>"Risk: Interpretation of content by the browser"
 * </ul>
 */
@Configuration
public class RestfulApiSecurityConfiguration {
  @Bean
  @ConditionalOnMissingBean(name = FrontendSecurityConfiguration.FRONTEND_SECURITY_ADVICE_BEAN_NAME)
  public SdaSecurityHeaders sdaSecurityHeaders() {
    return SdaSecurityType.RESTFUL_SECURITY::headers;
  }
}
