/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.mongodb.starter.converter;

import java.util.ArrayList;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
public class SdaMongoCustomConverterConfiguration {

  @Bean
  public MongoCustomConversions customConversions() {
    List<Converter<?, ?>> converters = new ArrayList<>();
    converters.add(ZonedDateTimeReadConverter.INSTANCE);
    converters.add(ZonedDateTimeWriteConverter.INSTANCE);
    return new MongoCustomConversions(converters);
  }
}
