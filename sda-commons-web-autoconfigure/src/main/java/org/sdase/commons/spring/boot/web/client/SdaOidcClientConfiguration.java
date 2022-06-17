/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.client;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.Provider;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientProperties.Registration;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientPropertiesRegistrationAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@AutoConfiguration
@ConditionalOnProperty(value = "oidc.client.enabled", havingValue = "true")
public class SdaOidcClientConfiguration {

  /**
   * This bean overwrites the {@code spring.security.oauth2.client}-based autoconfigured client
   * registrations to have more control about the initialization of the required beans for oidc
   * client authentication.
   *
   * @param clientId the oidc client id
   * @param clientSecret the oidc client secret
   * @param oidcIssuer the oidc issuer url
   * @return the client registration repository containing the oidc client registration.
   */
  @Bean
  InMemoryClientRegistrationRepository clientRegistrationRepository(
      @Value("${oidc.client.id}") String clientId,
      @Value("${oidc.client.secret}") String clientSecret,
      @Value("${oidc.client.issuer.uri}") String oidcIssuer) {
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

  @Bean
  OAuth2AuthorizedClientService authorizedClientService(
      ClientRegistrationRepository clientRegistrationRepository) {
    return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
  }

  @Bean
  public OAuth2AuthorizedClientManager authorizedClientManager(
      ClientRegistrationRepository clientRegistrationRepository,
      OAuth2AuthorizedClientService authorizedClientService) {
    return new AuthorizedClientServiceOAuth2AuthorizedClientManager(
        clientRegistrationRepository, authorizedClientService);
  }

  @Bean
  public OAuth2TokenProvider oAuth2Provider(
      OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager) {
    return new OAuth2TokenProvider(oAuth2AuthorizedClientManager);
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
