/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.opa;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
public class OpaRestTemplateConfiguration {

  private final Duration timeout;
  private final Duration connectionTimeout;

  /**
   * @param timeout the read timeout of the client that calls the Open Policy Agent server
   * @param connectionTimeout the connection timeout of the client that calls the Open Policy Agent
   *     server
   */
  public OpaRestTemplateConfiguration(
      @Value("${opa.client.timeout:500ms}") Duration timeout,
      @Value("${opa.client.connection.timeout:500ms}") Duration connectionTimeout) {
    this.timeout = timeout;
    this.connectionTimeout = connectionTimeout;
  }

  @Bean("opaRestTemplate")
  public RestTemplate opaRestTemplate(RestTemplateBuilder builder) {
    return builder.connectTimeout(connectionTimeout).readTimeout(timeout).build();
  }
}
