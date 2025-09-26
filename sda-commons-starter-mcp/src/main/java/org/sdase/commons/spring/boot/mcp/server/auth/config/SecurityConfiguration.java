/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth.config;

import static org.springframework.security.authorization.AuthenticatedAuthorizationManager.authenticated;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Set;
import org.sdase.commons.spring.boot.mcp.server.auth.opa.OpenPolicyAgentAuthorizationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(SecurityConfiguration.class);

  private final OpenPolicyAgentAuthorizationManager openPolicyAgentAuthorizationManager;

  private final AuthProperties authProperties;

  public SecurityConfiguration(
      OpenPolicyAgentAuthorizationManager openPolicyAgentAuthorizationManager,
      AuthProperties authProperties) {
    this.openPolicyAgentAuthorizationManager = openPolicyAgentAuthorizationManager;
    this.authProperties = authProperties;
  }

  @Bean
  @Order(1)
  @ConditionalOnBooleanProperty(
      prefix = "auth",
      name = "disable",
      havingValue = false,
      matchIfMissing = true)
  @SuppressWarnings("java:S4502")
  SecurityFilterChain authEnabledsecurityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            authorize ->
                authorize
                    .anyRequest()
                    .access(
                        AuthorizationManagers.allOf(
                            authenticated(), openPolicyAgentAuthorizationManager)))
        .oauth2ResourceServer(
            resourceServer ->
                resourceServer.authenticationManagerResolver(authenticationManagerResolver()))
        .csrf(CsrfConfigurer::disable) // MCP Server is not intended for browser based clients
        .cors(Customizer.withDefaults())
        .build();
  }

  @Bean
  @Order(2)
  @ConditionalOnBooleanProperty(
      prefix = "auth",
      name = "disable",
      havingValue = true,
      matchIfMissing = false)
  @SuppressWarnings("java:S4502")
  SecurityFilterChain authDisabledsecurityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(
            authorize -> authorize.anyRequest().access(openPolicyAgentAuthorizationManager))
        .csrf(CsrfConfigurer::disable)
        .cors(Customizer.withDefaults())
        .build();
  }

  /**
   * Creates an {@link AuthenticationManagerResolver} that either accepts anonymous requests (if no
   * issuers are configured) or validates JWT tokens from the configured issuers.
   */
  private AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver() {
    var issuers = authProperties.getIssuers();
    if (issuers.isEmpty()) {
      return createAnonymousOnlyResolver();
    } else {
      return createJwtIssuerResolver(issuers);
    }
  }

  private AuthenticationManagerResolver<HttpServletRequest> createJwtIssuerResolver(
      Set<String> issuers) {
    return JwtIssuerAuthenticationManagerResolver.fromTrustedIssuers(issuers);
  }

  private AuthenticationManagerResolver<HttpServletRequest> createAnonymousOnlyResolver() {
    LOG.warn("No issuers configured, allowing anonymous requests.");
    return context ->
        authentication -> {
          if (authentication
              instanceof org.springframework.security.authentication.AnonymousAuthenticationToken) {
            return authentication;
          }
          throw new BadCredentialsException("Invalid authentication for anonymous-only mode.");
        };
  }
}
