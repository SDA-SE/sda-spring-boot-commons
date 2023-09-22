/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AutoConfiguration
@PropertySource("classpath:/org/sdase/commons/spring/boot/web/client/default.properties")
public class SdaClientConfiguration implements WebMvcConfigurer {

  @Autowired ApplicationContext applicationContext;

  @Bean
  public AuthorizationStoreRequestInterceptor authorizationStoreRequestInterceptor() {
    return new AuthorizationStoreRequestInterceptor();
  }

  @Bean
  public AuthenticationHeaderExchangeFilter authenticationHeaderExchangeFilter() {
    return new AuthenticationHeaderExchangeFilter();
  }

  @Bean
  public OidcClientHttpExchangeFilter oidcClientHttpExchangeFilter() {
    return new OidcClientHttpExchangeFilter(applicationContext);
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authorizationStoreRequestInterceptor());
  }

  // TODO we need to be able to have multiple platform clients. Also it would be nice to have an
  // annotation like @PlatformClient

  /**
   * this defines a default client with a default baseUrl, the default filters for platform
   * context(like the interceptors for Feign) and the default headers
   *
   * @param baseUrl
   * @return a WebClient with the platform configuration
   */
  @Bean
  public WebClient platformClient(@Value("${platform.client.baseUrl}") String baseUrl) {
    return WebClient.builder()
        .baseUrl(baseUrl)
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .filter(authenticationHeaderExchangeFilter())
        .filter(oidcClientHttpExchangeFilter())
        .build();
  }

  /**
   * this is an example of using an annotated class for defining a client. Since it is an interface,
   * it needs a factory instantiation like this example, so Spring can instantiate it as a Bean. It
   * also needs a WebClient instance to derive from.
   *
   * @param platformClient
   * @return a WebClient from the interface
   */
  @Bean
  EmployeeClient postClient(WebClient platformClient) {
    HttpServiceProxyFactory httpServiceProxyFactory =
        HttpServiceProxyFactory.builder(WebClientAdapter.forClient(platformClient)).build();
    return httpServiceProxyFactory.createClient(EmployeeClient.class);
  }
}
