/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.ws.rs.core.HttpHeaders;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;

public class OidcClientRequestConfiguration {

  private final ApplicationContext applicationContext;

  public OidcClientRequestConfiguration(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Bean
  @Lazy
  @ConditionalOnProperty(value = "oidc.client.enabled", havingValue = "true")
  public RequestInterceptor getOidcRequestInterceptor() {
    return new OidcClientRequestInterceptor(applicationContext);
  }

  public static class OidcClientRequestInterceptor extends AuthHeaderClientInterceptor {

    private static final Logger LOG = LoggerFactory.getLogger(OidcClientRequestInterceptor.class);

    private final ApplicationContext applicationContext;

    public OidcClientRequestInterceptor(ApplicationContext applicationContext) {
      this.applicationContext = applicationContext;
    }

    @Override
    public void apply(RequestTemplate template) {
      firstAuthHeaderFromServletRequest()
          .or(
              () -> {
                try {
                  var bean =
                      applicationContext.getBean("oAuth2Provider", OAuth2TokenProvider.class);
                  return bean.getAuthenticationTokenForTechnicalUser("oidc");
                } catch (Exception e) {
                  LOG.error("Error retrieving OAuth 2 provider", e.getCause());
                }
                return Optional.empty();
              })
          .ifPresent(token -> template.header(HttpHeaders.AUTHORIZATION, token));
    }
  }
}
