/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mongodb;

import javax.net.ssl.SSLContext;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.mongodb.autoconfigure.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class MongoDbCaCertificatesConfiguration {

  @Bean
  @ConditionalOnBean(SSLContext.class)
  public MongoClientSettingsBuilderCustomizer sslMongoCustomizer(SSLContext sslContext) {
    // enabling ssl will be exclusively done by the options in the connection
    // string
    return clientSettingsBuilder ->
        clientSettingsBuilder.applyToSslSettings(builder -> builder.context(sslContext));
  }
}
