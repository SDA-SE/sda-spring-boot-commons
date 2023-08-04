/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.jsonschema.victools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.params.provider.Arguments.of;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.victools.jsonschema.generator.Option;
import com.github.victools.jsonschema.generator.OptionPreset;
import com.github.victools.jsonschema.generator.SchemaGenerator;
import com.github.victools.jsonschema.generator.SchemaGeneratorConfigBuilder;
import com.github.victools.jsonschema.generator.SchemaVersion;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TemporalFormatModuleTest {

  private static final Logger LOG = LoggerFactory.getLogger(TemporalFormatModuleTest.class);

  SchemaGenerator schemaGenerator =
      new SchemaGenerator(
          new SchemaGeneratorConfigBuilder(SchemaVersion.DRAFT_7, OptionPreset.PLAIN_JSON)
              .with(Option.INLINE_ALL_SCHEMAS) // to simplify testing
              .with(new TemporalFormatModule())
              .build());

  @MethodSource
  @ParameterizedTest
  void shouldIdentifyJsonSchemaFormat(Type givenType, String expectedFormat) {
    ObjectNode actual = schemaGenerator.generateSchema(givenType);
    LOG.info("Actual: {}", actual);
    JsonNode expected = expectedFormat == null ? null : new TextNode(expectedFormat);
    assertThat(actual.get("type")).isEqualTo(new TextNode("string"));
    assertThat(actual.get("format")).isEqualTo(expected);
  }

  static Stream<Arguments> shouldIdentifyJsonSchemaFormat() {
    return Stream.of(
        of(String.class, null),
        of(OffsetDateTime.class, "date-time"),
        of(Instant.class, "date-time"),
        of(ZonedDateTime.class, "date-time"),
        of(Date.class, "date-time"),
        of(LocalDate.class, "date"),
        of(Duration.class, "duration"),
        of(Period.class, "duration"),
        of(SomeEnum.class, null));
  }

  @MethodSource
  @ParameterizedTest
  void shouldIdentifyJsonSchemaFormatInFields(String givenField, String expectedFormat) {
    ObjectNode actual = schemaGenerator.generateSchema(AllTypes.class);
    LOG.info("Actual: {}", actual);
    JsonNode expected = expectedFormat == null ? null : new TextNode(expectedFormat);
    if (actual.get("properties") instanceof ObjectNode properties
        && properties.get(givenField) instanceof ObjectNode actualField) {
      assertThat(actualField.get("type")).isEqualTo(new TextNode("string"));
      assertThat(actualField.get("format")).isEqualTo(expected);
    } else {
      fail("Could not find /properties/{} in {}", givenField, actual);
    }
  }

  static Stream<Arguments> shouldIdentifyJsonSchemaFormatInFields() {
    return Stream.of(
        of("string", null),
        of("offsetDateTime", "date-time"),
        of("instant", "date-time"),
        of("zonedDateTime", "date-time"),
        of("date", "date-time"),
        of("localDate", "date"),
        of("duration", "duration"),
        of("period", "duration"),
        of("someEnum", null));
  }

  enum SomeEnum {
    SOME_VALUE,
    OTHER_VALUE
  }

  @SuppressWarnings("unused")
  static class AllTypes {

    private String string;
    private OffsetDateTime offsetDateTime;
    private Instant instant;
    private ZonedDateTime zonedDateTime;
    private Date date;
    private LocalDate localDate;
    private Duration duration;
    private Period period;
    private SomeEnum someEnum;

    public String getString() {
      return string;
    }

    public AllTypes setString(String string) {
      this.string = string;
      return this;
    }

    public OffsetDateTime getOffsetDateTime() {
      return offsetDateTime;
    }

    public AllTypes setOffsetDateTime(OffsetDateTime offsetDateTime) {
      this.offsetDateTime = offsetDateTime;
      return this;
    }

    public Instant getInstant() {
      return instant;
    }

    public AllTypes setInstant(Instant instant) {
      this.instant = instant;
      return this;
    }

    public ZonedDateTime getZonedDateTime() {
      return zonedDateTime;
    }

    public AllTypes setZonedDateTime(ZonedDateTime zonedDateTime) {
      this.zonedDateTime = zonedDateTime;
      return this;
    }

    public Date getDate() {
      return date;
    }

    public AllTypes setDate(Date date) {
      this.date = date;
      return this;
    }

    public LocalDate getLocalDate() {
      return localDate;
    }

    public AllTypes setLocalDate(LocalDate localDate) {
      this.localDate = localDate;
      return this;
    }

    public Duration getDuration() {
      return duration;
    }

    public AllTypes setDuration(Duration duration) {
      this.duration = duration;
      return this;
    }

    public Period getPeriod() {
      return period;
    }

    public AllTypes setPeriod(Period period) {
      this.period = period;
      return this;
    }

    public SomeEnum getSomeEnum() {
      return someEnum;
    }

    public AllTypes setSomeEnum(SomeEnum someEnum) {
      this.someEnum = someEnum;
      return this;
    }
  }
}
