/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.Provider;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.Registration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@ConditionalOnProperty(value = "oidc.client.enabled", havingValue = "true")
public class SdaOidcClientConfiguration {

  /** the oidc client id */
  @Value("${oidc.client.id}")
  private String clientId;
  /** the oidc client secret */
  @Value("${oidc.client.secret}")
  private String clientSecret;
  /** the oidc client issuer URI */
  @Value("${oidc.client.issuer.uri}")
  private String oidcIssuer;

  /**
   * Creates and returns an instance of {@link OAuth2TokenProvider}. It calls the issuer URI to
   * retrieve the OAuth information. In case it already happened, it will not call the endpoint
   * again, but return the oAuth2TokenProvider right away. When it fails to gather information on
   * the issuer endpoint it will return a null object, so it will try again in the next request
   *
   * @throws IllegalArgumentException if the issuer endpoint is not available
   * @return oAuth2TokenProvider
   */
  @Bean
  @Lazy
  public OAuth2TokenProvider oAuth2Provider() throws IllegalArgumentException {
    return new OAuth2TokenProvider(authorizedClientManager());
  }

  private OAuth2AuthorizedClientManager authorizedClientManager() {
    return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
        clientRegistrationRepository(), authorizedClientService());
  }

  private OAuth2AuthorizedClientService authorizedClientService() {
    return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository());
  }

  private InMemoryClientRegistrationRepository clientRegistrationRepository() {
    var oAuth2ClientProperties = new OAuth2ClientProperties();
    Registration registration = buildOidcClientRegistration(clientId, clientSecret);
    Provider provider = buildOidcProvider(oidcIssuer);
    oAuth2ClientProperties.getRegistration().put("oidc", registration);
    oAuth2ClientProperties.getProvider().put("oidc", provider);
    oAuth2ClientProperties.validate();
    List<ClientRegistration> registrations =
        new ArrayList<>(
            OAuth2ClientPropertiesRegistrationAdapter.getClientRegistrations(oAuth2ClientProperties)
                .values());
    return new InMemoryClientRegistrationRepository(registrations);
  }

  private Provider buildOidcProvider(String oidcIssuer) {
    var provider = new Provider();
    provider.setIssuerUri(oidcIssuer);
    return provider;
  }

  private Registration buildOidcClientRegistration(String clientId, String clientSecret) {
    var registration = new Registration();
    registration.setClientName("oidc");
    registration.setClientId(clientId);
    registration.setClientSecret(clientSecret);
    registration.setAuthorizationGrantType("client_credentials");
    return registration;
  }
}
