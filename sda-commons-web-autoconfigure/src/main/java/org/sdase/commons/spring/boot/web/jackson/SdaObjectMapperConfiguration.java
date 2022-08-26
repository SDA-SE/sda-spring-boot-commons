/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.jackson;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.time.ZonedDateTime;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@AutoConfiguration
public class SdaObjectMapperConfiguration {
  @Bean
  Jackson2ObjectMapperBuilder objectMapperBuilder() {
    return new Jackson2ObjectMapperBuilder()
        .featuresToDisable(
            SerializationFeature.FAIL_ON_EMPTY_BEANS,
            SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,
            SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS,
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES,
            DeserializationFeature.FAIL_ON_INVALID_SUBTYPE)
        .featuresToEnable(
            DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
            DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL,
            DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE)
        .modulesToInstall(
            new SimpleModule().addSerializer(ZonedDateTime.class, new Iso8601Serializer()),
            new Jdk8Module(),
            new JavaTimeModule(),
            new ParameterNamesModule());
  }
}
