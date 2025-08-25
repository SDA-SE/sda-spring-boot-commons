/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.auth.opa;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
public class OpaRestTemplateConfiguration {

  @Bean
  @Qualifier("opaRestTemplate")
  public RestTemplate opaRestTemplate(
      RestTemplateBuilder restTemplateBuilder, @Value("${opa.timeout:PT5S}") Duration opaTimeout) {
    return restTemplateBuilder.connectTimeout(opaTimeout).readTimeout(opaTimeout).build();
  }
}
