/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.jackson;

import java.time.ZonedDateTime;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import tools.jackson.core.StreamWriteFeature;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.cfg.EnumFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

@AutoConfiguration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SdaObjectMapperConfiguration {
  public static final String OBJECT_MAPPER_BUILDER_BEAN_NAME = "sdaObjectMapperBuilder";

  @Bean(name = OBJECT_MAPPER_BUILDER_BEAN_NAME)
  @Primary
  public JsonMapper.Builder sdaObjectMapperBuilder() {
    return JsonMapper.builder()
        .disable(
            DeserializationFeature.FAIL_ON_INVALID_SUBTYPE,
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
        .disable(StreamWriteFeature.AUTO_CLOSE_CONTENT)
        .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
        .disable(
            DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS,
            DateTimeFeature.WRITE_DURATIONS_AS_TIMESTAMPS)
        .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        .enable(
            EnumFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL,
            EnumFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
        .addModule(new SimpleModule().addSerializer(ZonedDateTime.class, new Iso8601Serializer()));
  }
}
