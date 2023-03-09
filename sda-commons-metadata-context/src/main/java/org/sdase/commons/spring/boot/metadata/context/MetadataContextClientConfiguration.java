/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.metadata.context;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;

public class MetadataContextClientConfiguration {

  @Bean
  public RequestInterceptor metadataContextClientInterceptor() {
    return new MetadataContextClientInterceptor(MetadataContext.metadataFields());
  }
}
