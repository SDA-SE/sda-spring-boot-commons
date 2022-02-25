/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.auth;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.server.resource.authentication.JwtIssuerAuthenticationManagerResolver;
import org.springframework.util.StringUtils;

@EnableWebSecurity
@ComponentScan
@Order(1)
public class SdaSecurityConfiguration extends WebSecurityConfigurerAdapter {

  private static final Logger LOG = LoggerFactory.getLogger(SdaSecurityConfiguration.class);
  private final String issuers;

  private final boolean disableAuthentication;

  private final SdaAccessDecisionManager sdaAccessDecisionManager;

  /**
   * @param issuers Comma separated string of open id discovery key sources with required issuers.
   * @param disableAuthentication Disables all authentication
   * @param sdaAccessDecisionManager {@link
   *     org.springframework.security.access.AccessDecisionManager} that decides about authorization
   */
  public SdaSecurityConfiguration(
      @Value("${auth.issuers:}") String issuers,
      @Value("${auth.disable:false}") boolean disableAuthentication,
      SdaAccessDecisionManager sdaAccessDecisionManager) {
    this.issuers = issuers;
    this.disableAuthentication = disableAuthentication;
    this.sdaAccessDecisionManager = sdaAccessDecisionManager;
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    if (disableAuthentication) {
      LOG.warn("Authentication is disabled. This setting should NEVER be used in production.");
      noAuthentication(http);
    } else {
      LOG.info("Configured to accept these issuers: {}", issuers);
      oidcAuthentication(http);
    }
  }

  private void oidcAuthentication(HttpSecurity http) throws Exception {
    var authenticationManagerResolver = createAuthenticationManagerResolver();
    http.csrf()
        .disable() // NOSONAR
        .authorizeRequests(
            authorize ->
                authorize.anyRequest().permitAll().accessDecisionManager(sdaAccessDecisionManager))
        .oauth2ResourceServer(
            oauth2 -> oauth2.authenticationManagerResolver(authenticationManagerResolver));
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
    http.csrf()
        .disable() // NOSONAR
        .authorizeRequests(
            authorize ->
                authorize.anyRequest().permitAll().accessDecisionManager(sdaAccessDecisionManager));
  }

  private List<String> commaSeparatedStringToList(String issuers) {
    return Stream.of(issuers.split(","))
        .filter(StringUtils::hasText)
        .map(String::trim)
        .collect(Collectors.toList());
  }
}
