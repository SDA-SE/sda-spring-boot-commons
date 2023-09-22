/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client;

import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import reactor.core.publisher.Mono;

/**
 * This is an alternative to the Interceptors used with Feign clients. It will be executed against
 * every request on the configured WebClient.
 */
@Component
public class OidcClientHttpExchangeFilter implements ExchangeFilterFunction {

  private static final Logger LOG = LoggerFactory.getLogger(OidcClientHttpExchangeFilter.class);

  private final ApplicationContext applicationContext;

  public OidcClientHttpExchangeFilter(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest clientRequest, ExchangeFunction nextFilter) {

    var token = firstAuthHeaderFromServletRequest();
    if (token.isPresent()) {
      // Create a new ClientRequest with the additional headers
      ClientRequest modifiedRequest =
          ClientRequest.from(clientRequest)
              .header(org.springframework.http.HttpHeaders.AUTHORIZATION, token.get())
              .build();

      return nextFilter.exchange(modifiedRequest);
    }

    try {
      var bean = applicationContext.getBean("oAuth2Provider", OAuth2TokenProvider.class);

      var generatedToken = bean.getAuthenticationTokenForTechnicalUser("oidc");

      if (generatedToken.isPresent()) {
        ClientRequest modifiedRequest =
            ClientRequest.from(clientRequest)
                .header(org.springframework.http.HttpHeaders.AUTHORIZATION, generatedToken.get())
                .build();
        return nextFilter.exchange(modifiedRequest);
      }
    } catch (Exception e) {
      LOG.error("Error retrieving OAuth 2 provider", e.getCause());
    }
    return nextFilter.exchange(clientRequest);
  }

  Optional<String> firstAuthHeaderFromServletRequest() {
    try {
      var attribute =
          RequestContextHolder.currentRequestAttributes()
              .getAttribute(
                  AuthorizationStoreRequestInterceptor.ATTRIBUTE_NAME,
                  AuthorizationStoreRequestInterceptor.SCOPE);
      if (attribute instanceof String strAttribute) {
        return Optional.of(strAttribute);
      } else {
        return Optional.empty();
      }
    } catch (Exception e) {
      LOG.info("No authentication header: Not in a request context.");
      return Optional.empty();
    }
  }
}
