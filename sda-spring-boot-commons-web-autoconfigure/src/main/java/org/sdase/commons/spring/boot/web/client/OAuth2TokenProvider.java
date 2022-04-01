/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.client;

import java.util.Optional;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

public class OAuth2TokenProvider {

  private final OAuth2AuthorizedClientManager authorizedClientManager;

  public OAuth2TokenProvider(OAuth2AuthorizedClientManager authorizedClientManager) {
    this.authorizedClientManager = authorizedClientManager;
  }

  public Optional<String> getAuthenticationTokenForTechnicalUser(String clientRegistrationId) {
    return Optional.ofNullable(
            authorizedClientManager.authorize(createOAuth2AuthorizeRequest(clientRegistrationId)))
        .map(OAuth2AuthorizedClient::getAccessToken)
        .map(OAuth2AccessToken::getTokenValue)
        .map(token -> "Bearer " + token);
  }

  private OAuth2AuthorizeRequest createOAuth2AuthorizeRequest(String clientRegistrationId) {
    return OAuth2AuthorizeRequest.withClientRegistrationId(clientRegistrationId)
        .principal("org.sdase.commons.oidc.client")
        .build();
  }
}
