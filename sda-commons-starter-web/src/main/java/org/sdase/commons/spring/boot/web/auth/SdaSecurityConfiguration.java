/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.sdase.commons.spring.boot.web.monitoring.SdaMonitoringConfiguration;
import org.sdase.commons.spring.boot.web.security.headers.SdaSecurityHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.util.StringUtils;

@EnableWebSecurity
@AutoConfiguration(after = SdaMonitoringConfiguration.class)
@ComponentScan
@Order(1)
public class SdaSecurityConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(SdaSecurityConfiguration.class);
  private final String issuers;

  private final boolean disableAuthentication;

  private final SdaAuthorizationManager sdaAccessDecisionManager;

  private final SdaSecurityHeaders sdaSecurityHeaders;

  /**
   * @param issuers Comma separated string of open id discovery key sources with required issuers.
   * @param disableAuthentication Disables all authentication
   * @param sdaAccessDecisionManager {@link
   *     org.springframework.security.authorization.AuthorizationManager} that decides about
   *     authorization
   * @param sdaSecurityHeaders static response headers that should be added for security reasons
   */
  public SdaSecurityConfiguration(
      @Value("${auth.issuers:}") String issuers,
      @Value("${auth.disable:false}") boolean disableAuthentication,
      SdaAuthorizationManager sdaAccessDecisionManager,
      Optional<SdaSecurityHeaders> sdaSecurityHeaders) {
    this.issuers = issuers;
    this.disableAuthentication = disableAuthentication;
    this.sdaAccessDecisionManager = sdaAccessDecisionManager;
    this.sdaSecurityHeaders = sdaSecurityHeaders.orElse(List::of);
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    if (disableAuthentication) {
      LOG.warn("Authentication is disabled. This setting should NEVER be used in production.");
      noAuthentication(http);
    } else {
      LOG.info("Configured to accept these issuers: {}", issuers);
      oidcAuthentication(http);
    }
    return http.build();
  }

  @Bean
  // Avoid generating a user with basic credentials
  AuthenticationManager authenticationManager(AuthenticationManagerBuilder auth) {
    return auth.getOrBuild();
  }

  private void oidcAuthentication(HttpSecurity http) throws Exception {
    var authenticationManagerResolver = createAuthenticationManagerResolver();

    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize -> authorize.requestMatchers("/**").access(sdaAccessDecisionManager))
        .oauth2ResourceServer(
            oauth2 -> oauth2.authenticationManagerResolver(authenticationManagerResolver))
        .headers(
            configurer ->
                configurer.addHeaderWriter(
                    new StaticHeadersWriter(sdaSecurityHeaders.getSecurityHeaders())));
  }

  private AuthenticationManagerResolver<HttpServletRequest> createAuthenticationManagerResolver() {
    var trustedIssuers = commaSeparatedStringToList(this.issuers);
    if (trustedIssuers.isEmpty()) {
      LOG.warn("No trusted issuers configured, anonymous requests allowed.");
      return onlyAnonymousAuthenticationManagerResolver();
    } else {
      return new JwtIssuerAuthenticationManagerResolver(trustedIssuers);
    }
  }

  private AuthenticationManagerResolver<HttpServletRequest>
      onlyAnonymousAuthenticationManagerResolver() {
    return context ->
        authentication -> {
          if (authentication instanceof AnonymousAuthenticationToken) {
            return authentication;
          }
          throw new BadCredentialsException("Invalid authentication");
        };
  }

  private void noAuthentication(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(
            authorize -> authorize.requestMatchers("/**").access(sdaAccessDecisionManager))
        .headers(
            configurer ->
                configurer.addHeaderWriter(
                    new StaticHeadersWriter(sdaSecurityHeaders.getSecurityHeaders())));
  }

  private List<String> commaSeparatedStringToList(String issuers) {
    return Stream.of(issuers.split(",")).filter(StringUtils::hasText).map(String::trim).toList();
  }
}
