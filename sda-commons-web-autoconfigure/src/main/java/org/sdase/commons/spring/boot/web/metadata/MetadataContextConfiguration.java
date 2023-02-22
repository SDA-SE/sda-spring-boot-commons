/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class MetadataContextConfiguration {

  @Bean
  public MetadataContextClientRequestFilter metadataContextClientRequestFilter() {
    return new MetadataContextClientRequestFilter(MetadataContext.metadataFields());
  }

  @Bean
  public MetadataContextFilter metadataContextFilter() {
    return new MetadataContextFilter(MetadataContext.metadataFields());
  }
}
