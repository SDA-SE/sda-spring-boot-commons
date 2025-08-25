package org.sdase.commons.spring.boot.mcp.auth;

import static org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer.authorizationServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@AutoConfiguration
public class SdaWebSecurityConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(SdaWebSecurityConfiguration.class);

  private final String issuers;
  private final boolean disableAuthentication;

  public SdaWebSecurityConfiguration(
      @Value("${auth.issuers:}") String issuers,
      @Value("${auth.disable:false}") boolean disableAuthentication) {
    this.issuers = issuers;
    this.disableAuthentication = disableAuthentication;
  }

  @Bean
  SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
        .with(authorizationServer(), Customizer.withDefaults())
        .oauth2ResourceServer(resource -> resource.jwt(Customizer.withDefaults()))
        .csrf(CsrfConfigurer::disable)
        .cors(Customizer.withDefaults())
        .build();
  }
}
