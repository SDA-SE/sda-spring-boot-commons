/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.jsonschema.victools;

import com.github.victools.jsonschema.generator.Module;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.TypeScope;
import com.github.victools.jsonschema.generator.impl.module.SimpleTypeModule;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Set;

/**
 * A module that adds the correct {@code string} type and {@code format} to time related types
 * wherever they are used according to the configuration of the {@link
 * com.fasterxml.jackson.databind.ObjectMapper} in this library. See also {@code
 * org.sdase.commons.spring.boot.web.jackson.SdaObjectMapperConfiguration}.
 */
public class TemporalFormatModule implements Module {

  private static final Set<Class<?>> DATE_TIME_TYPES =
      Set.of(OffsetDateTime.class, Instant.class, ZonedDateTime.class, Date.class);
  private static final Set<Class<?>> DATE_TYPES = Set.of(LocalDate.class);
  private static final Set<Class<?>> DURATION_TYPES = Set.of(Duration.class, Period.class);

  @Override
  public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
    builder.with(createTemporalTypesAsStringModule());
    builder.forTypesInGeneral().withStringFormatResolver(this::stringFormatResolver);
  }

  private String stringFormatResolver(TypeScope target) {
    var declaredType = target.getType();
    if (DATE_TIME_TYPES.stream().anyMatch(declaredType::isInstanceOf)) {
      return "date-time";
    }
    if (DATE_TYPES.stream().anyMatch(declaredType::isInstanceOf)) {
      return "date";
    }
    if (DURATION_TYPES.stream().anyMatch(declaredType::isInstanceOf)) {
      return "duration";
    }
    return null;
  }

  private static SimpleTypeModule createTemporalTypesAsStringModule() {
    SimpleTypeModule temporalTypesAsStringModule = new SimpleTypeModule();
    DATE_TIME_TYPES.forEach(temporalTypesAsStringModule::withStringType);
    DATE_TYPES.forEach(temporalTypesAsStringModule::withStringType);
    DURATION_TYPES.forEach(temporalTypesAsStringModule::withStringType);
    return temporalTypesAsStringModule;
  }
}
