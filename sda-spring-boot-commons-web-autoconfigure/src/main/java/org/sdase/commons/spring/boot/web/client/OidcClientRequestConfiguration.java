/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import javax.ws.rs.core.HttpHeaders;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OidcClientRequestConfiguration {

  private final OAuth2TokenProvider oAuth2TokenProvider;

  public OidcClientRequestConfiguration(ObjectProvider<OAuth2TokenProvider> oAuth2TokenProvider) {
    this.oAuth2TokenProvider = oAuth2TokenProvider.getIfAvailable();
  }

  @Bean
  @ConditionalOnProperty(value = "oidc.client.enabled", havingValue = "true")
  public RequestInterceptor getOidcRequestInterceptor() {
    return new OidcClientRequestInterceptor(oAuth2TokenProvider);
  }

  public static class OidcClientRequestInterceptor extends AuthHeaderClientInterceptor {

    private final OAuth2TokenProvider oAuth2TokenProvider;

    public OidcClientRequestInterceptor(OAuth2TokenProvider oAuth2TokenProvider) {
      this.oAuth2TokenProvider = oAuth2TokenProvider;
    }

    @Override
    public void apply(RequestTemplate template) {
      firstAuthHeaderFromServletRequest()
          .or(() -> oAuth2TokenProvider.getAuthenticationTokenForTechnicalUser("oidc"))
          .ifPresent(token -> template.header(HttpHeaders.AUTHORIZATION, token));
    }
  }
}
