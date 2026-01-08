/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assumptions.assumeThat;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.jackson.Iso8601Serializer.WithMillis;
import org.sdase.commons.spring.boot.web.jackson.Iso8601Serializer.WithNanos;
import org.sdase.commons.spring.boot.web.jackson.test.JacksonTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import tools.jackson.core.Version;
import tools.jackson.core.json.PackageVersion;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.annotation.JsonSerialize;

@SpringBootTest(classes = JacksonTestApp.class)
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
class DateFormatObjectMapperTest {

  @Autowired private ObjectMapper om;

  @Test
  void readIso8601DateAsDate() {
    String given = asJsonString("2018-11-21");

    Date date = om.readValue(given, Date.class);

    assertThat(date).hasYear(2018).hasMonth(11).hasDayOfMonth(21);
  }

  @Test
  void readIso8601DateAsLocalDate() {
    String given = asJsonString("2018-11-21");

    LocalDate date = om.readValue(given, LocalDate.class);

    assertThat(date).isEqualTo("2018-11-21");
  }

  @Test
  void readIso8601DateTimeAsLocalDate() {
    String given = asJsonString("2018-11-21T13:16:47Z");

    LocalDate date = om.readValue(given, LocalDate.class);

    assertThat(date).isEqualTo("2018-11-21");
  }

  @Test
  void readIso8601UtcTimeAsDate() {
    String given = asJsonString("2018-11-21T13:16:47Z");

    Date date = om.readValue(given, Date.class);

    // normalize from default timezone to UTC
    // this is why Date is not good for APIs
    ZoneOffset zoneOffset =
        ZoneOffset.systemDefault().getRules().getOffset(Instant.ofEpochMilli(date.getTime()));
    date = new Date(date.getTime() - 1_000L * zoneOffset.getTotalSeconds());

    assertThat(date)
        .hasYear(2018)
        .hasMonth(11)
        .hasDayOfMonth(21)
        .hasHourOfDay(13)
        .hasMinute(16)
        .hasSecond(47);
  }

  @Test
  void readIso8601UtcTimeAsZonedDateTime() { // NOSONAR not parameterized
    String given = asJsonString("2018-11-21T13:16:47Z");

    ZonedDateTime date = om.readValue(given, ZonedDateTime.class);

    assertThat(date).isEqualTo("2018-11-21T13:16:47+00:00");
    assertThat(date.get(ChronoField.OFFSET_SECONDS)).isZero();
  }

  @Test
  void readIso8601UtcTimeAsZonedDateTimeWithNamedZone() {
    String given = asJsonString("2018-01-25T00:00Z[UTC]");

    ZonedDateTime date = om.readValue(given, ZonedDateTime.class);

    assertThat(date).isEqualTo("2018-01-25T00:00:00+00:00");
  }

  @Test
  void readIso8601CetTimeAsZonedDateTimeWithNamedZone() {
    String given = asJsonString("2018-01-09T00:00+01:00[Europe/Paris]");

    ZonedDateTime date = om.readValue(given, ZonedDateTime.class);

    assertThat(date).isEqualTo("2018-01-09T00:00:00+01:00").isEqualTo("2018-01-08T23:00:00+00:00");
  }

  @Test
  void readIso8601UtcTimeWithMillisAsZonedDateTime() {
    String given = asJsonString("2018-11-21T13:16:47.647Z");

    ZonedDateTime date = om.readValue(given, ZonedDateTime.class);

    assertThat(date).isEqualTo("2018-11-21T13:16:47.647+00:00");
    assertThat(date.get(ChronoField.OFFSET_SECONDS)).isZero();
  }

  @Test
  void readIso8601ZeroOffsetWithColonTimeAsZonedDateTime() {
    String given = asJsonString("2018-11-21T13:16:47+00:00");

    ZonedDateTime date = om.readValue(given, ZonedDateTime.class);

    assertThat(date).isEqualTo("2018-11-21T13:16:47+00:00");
    assertThat(date.get(ChronoField.OFFSET_SECONDS)).isZero();
  }

  @Test
  void readIso8601CetTimeAsZonedDateTime() {
    String given = asJsonString("2018-11-21T14:16:47+01:00");

    ZonedDateTime date = om.readValue(given, ZonedDateTime.class);

    assertThat(date).isEqualTo("2018-11-21T13:16:47+00:00");
    assertThat(date.get(ChronoField.OFFSET_SECONDS)).isZero();
  }

  @Test
  void readIso8601CetTimeAsZonedDateTimeWithoutColon() {
    // Skip test until Bugfix in Jackson
    // see https://github.com/FasterXML/jackson-modules-java8/issues/38
    var jacksonVersion = PackageVersion.VERSION;
    var minJacksonVersion =
        new Version(2, 13, 0, null, jacksonVersion.getGroupId(), jacksonVersion.getArtifactId());
    assumeThat(jacksonVersion).isGreaterThanOrEqualTo(minJacksonVersion);

    String given = asJsonString("2018-11-21T14:16:47+0100");

    ZonedDateTime date = om.readValue(given, ZonedDateTime.class);

    assertThat(date).isEqualTo("2018-11-21T13:16:47+00:00");
    assertThat(date.get(ChronoField.OFFSET_SECONDS)).isZero();
  }

  @Test
  void readIso8601CetTimeAsDateWithoutColon() {
    String given = asJsonString("2018-11-21T14:16:47+0100");

    Date date = om.readValue(given, Date.class);

    assertThat(date).isEqualTo("2018-11-21T13:16:47+00:00");
  }

  @Test
  void readIso8601CetTimeAsZonedDateTimeWithNanos() {
    String given = asJsonString("2018-11-21T14:16:47.9650003+01:00");

    ZonedDateTime date = om.readValue(given, ZonedDateTime.class);

    assertThat(date).isEqualTo("2018-11-21T13:16:47.9650003+00:00");
    assertThat(date.get(ChronoField.OFFSET_SECONDS)).isZero();
  }

  @Test
  void readIso8601Duration() {
    String given = asJsonString("P1DT13M");

    Duration duration = om.readValue(given, Duration.class);

    assertThat(duration).isEqualTo(Duration.ofDays(1).plus(Duration.ofMinutes(13)));
  }

  @Test
  void readIso8601Period() {
    String given = asJsonString("P1Y35D");

    Period period = om.readValue(given, Period.class);

    assertThat(period).isEqualTo(Period.ofYears(1).plus(Period.ofDays(35)));
  }

  @Test
  void writeIso8601UtcTime() {
    ZonedDateTime given =
        ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47), ZoneOffset.UTC);

    String actual = om.writeValueAsString(given);

    assertThat(actual).isEqualTo(asJsonString("2018-11-21T13:16:47Z"));
  }

  @Test
  void writeIso8601CetTime() {
    ZonedDateTime given =
        ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47), ZoneId.of("CET"));

    String actual = om.writeValueAsString(given);

    assertThat(actual).isEqualTo(asJsonString("2018-11-21T13:16:47+01:00"));
  }

  @Test
  void writeIso8601CetTimeWithMillis() {
    var given =
        new Object() {
          @JsonSerialize(using = WithMillis.class)
          private final ZonedDateTime zdt =
              ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47), ZoneId.of("CET"))
                  .plus(965, ChronoUnit.MILLIS);

          @SuppressWarnings("unused")
          public ZonedDateTime getZdt() {
            return zdt;
          }
        };

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47.965+01:00"));
  }

  @Test
  void writeIso8601CetTimeWithNanos() {
    var given =
        new Object() {
          @JsonSerialize(using = WithNanos.class)
          private final ZonedDateTime zdt =
              ZonedDateTime.of(
                  LocalDateTime.of(2018, 11, 21, 13, 16, 47, 965_000_300), ZoneId.of("CET"));

          @SuppressWarnings("unused")
          public ZonedDateTime getZdt() {
            return zdt;
          }
        };

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47.965000300+01:00"));
  }

  @Test
  void writeIso8601DateFromLocalDate() {
    LocalDate date = LocalDate.of(2018, 11, 21);

    String actual = om.writeValueAsString(date);

    assertThat(actual).isEqualTo(asJsonString("2018-11-21"));
  }

  @Test
  void writeIso8601WithMillisCetIfMillisSet() {
    DateTimeHolder given =
        new DateTimeHolder()
            .setZonedDateTimeWithMillis(
                ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47), ZoneId.of("CET"))
                    .plus(965, ChronoUnit.MILLIS));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47.965+01:00"));
  }

  @Test
  void writeIso8601WithSecondsCetIfOnlyMinutesSet() {
    ZonedDateTime given =
        ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16), ZoneId.of("CET"));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:00+01:00"));
  }

  @Test
  void writeIso8601WithMillisCetIfNanosSet() {
    DateTimeHolder given =
        new DateTimeHolder()
            .setZonedDateTimeWithMillis(
                ZonedDateTime.of(
                    LocalDateTime.of(2018, 11, 21, 13, 16, 47, 965_000_300), ZoneId.of("CET")));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47.965+01:00"));
  }

  @Test
  void writeIso8601UsingConfigWithMillisCetIfNanosSet() {

    var given =
        new Object() {
          @JsonSerialize(using = WithMillis.class)
          private final ZonedDateTime zdt =
              ZonedDateTime.of(
                  LocalDateTime.of(2018, 11, 21, 13, 16, 47, 965_000_300), ZoneId.of("CET"));

          @SuppressWarnings("unused")
          public ZonedDateTime getZdt() {
            return zdt;
          }
        };

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47.965+01:00"));
  }

  @Test
  void writeIso8601UsingConfigWithSecondsCetIfNanosSet() {
    ZonedDateTime given =
        ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47, 965_000_300), ZoneId.of("CET"));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47+01:00"));
  }

  @Test
  void writeIso8601WithMillisCetIfSecondsSet() {
    DateTimeHolder given =
        new DateTimeHolder()
            .setZonedDateTimeWithMillis(
                ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47), ZoneId.of("CET")));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47.000+01:00"));
  }

  @Test
  void writeIso8601WithMillisUtcIfMillisSet() {
    DateTimeHolder given =
        new DateTimeHolder()
            .setZonedDateTimeWithMillis(
                ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47), ZoneOffset.UTC)
                    .plus(965, ChronoUnit.MILLIS));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47.965Z"));
  }

  @Test
  void writeIso8601WithMillisUtcIfNanosSet() {
    DateTimeHolder given =
        new DateTimeHolder()
            .setZonedDateTimeWithMillis(
                ZonedDateTime.of(
                    LocalDateTime.of(2018, 11, 21, 13, 16, 47, 965_000_300), ZoneOffset.UTC));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47.965Z"));
  }

  @Test
  void writeIso8601WithMillisUtcIfSecondsSet() {
    DateTimeHolder given =
        new DateTimeHolder()
            .setZonedDateTimeWithMillis(
                ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47), ZoneOffset.UTC));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47.000Z"));
  }

  @Test
  void readIso8601WithNanosInPropertyFormattedWithMillis() {
    String given =
        '{'
            + asJsonString("zonedDateTimeWithMillis")
            + ':'
            + asJsonString("2018-11-21T13:16:47.9650003Z")
            + '}';

    DateTimeHolder dateTimeHolder = om.readValue(given, DateTimeHolder.class);

    assertThat(dateTimeHolder.getZonedDateTimeWithMillis())
        .isEqualTo("2018-11-21T13:16:47.9650003Z");
  }

  @Test
  void readIso8601WithSecondsInPropertyFormattedWithMillis() {
    String given =
        '{'
            + asJsonString("zonedDateTimeWithMillis")
            + ':'
            + asJsonString("2018-11-21T13:16:47Z")
            + '}';

    DateTimeHolder dateTimeHolder = om.readValue(given, DateTimeHolder.class);

    assertThat(dateTimeHolder.getZonedDateTimeWithMillis()).isEqualTo("2018-11-21T13:16:47Z");
  }

  @Test
  void writeIso8601WithSecondsCetIfMillisSet() {
    DateTimeHolder given =
        new DateTimeHolder()
            .setZonedDateTime(
                ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47), ZoneId.of("CET"))
                    .plus(965, ChronoUnit.MILLIS));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47+01:00"));
  }

  @Test
  void writeIso8601WithSecondsCetIfNanosSet() {
    DateTimeHolder given =
        new DateTimeHolder()
            .setZonedDateTime(
                ZonedDateTime.of(
                    LocalDateTime.of(2018, 11, 21, 13, 16, 47, 965_000_300), ZoneId.of("CET")));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47+01:00"));
  }

  @Test
  void writeIso8601WithSecondsCetIfSecondsSet() {
    DateTimeHolder given =
        new DateTimeHolder()
            .setZonedDateTime(
                ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47), ZoneId.of("CET")));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47+01:00"));
  }

  @Test
  void writeIso8601WithSecondsUtcIfMillisSet() {
    DateTimeHolder given =
        new DateTimeHolder()
            .setZonedDateTime(
                ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47), ZoneOffset.UTC)
                    .plus(965, ChronoUnit.MILLIS));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47Z"));
  }

  @Test
  void writeIso8601WithSecondsUtcIfNanosSet() {
    DateTimeHolder given =
        new DateTimeHolder()
            .setZonedDateTime(
                ZonedDateTime.of(
                    LocalDateTime.of(2018, 11, 21, 13, 16, 47, 965_000_300), ZoneOffset.UTC));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47Z"));
  }

  @Test
  void writeIso8601WithSecondsUtcIfSecondsSet() {
    DateTimeHolder given =
        new DateTimeHolder()
            .setZonedDateTime(
                ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47), ZoneOffset.UTC));

    String actual = om.writeValueAsString(given);

    assertThat(actual).contains(asJsonString("2018-11-21T13:16:47Z"));
  }

  @Test
  void readIso8601WithNanosInPropertyFormattedWithSeconds() {
    String given =
        '{'
            + asJsonString("zonedDateTime")
            + ':'
            + asJsonString("2018-11-21T13:16:47.9650003Z")
            + '}';

    DateTimeHolder dateTimeHolder = om.readValue(given, DateTimeHolder.class);

    assertThat(dateTimeHolder.getZonedDateTime()).isEqualTo("2018-11-21T13:16:47.9650003Z");
  }

  @Test
  void readIso8601WithSecondsInPropertyFormattedWithSeconds() {
    String given =
        '{' + asJsonString("zonedDateTime") + ':' + asJsonString("2018-11-21T13:16:47Z") + '}';

    DateTimeHolder dateTimeHolder = om.readValue(given, DateTimeHolder.class);

    assertThat(dateTimeHolder.getZonedDateTime()).isEqualTo("2018-11-21T13:16:47Z");
  }

  private String asJsonString(String rawString) {
    return '"' + rawString + '"';
  }

  private static class DateTimeHolder {

    // default Iso8601Serializer
    private ZonedDateTime zonedDateTime;

    @JsonSerialize(using = WithMillis.class)
    private ZonedDateTime zonedDateTimeWithMillis;

    public ZonedDateTime getZonedDateTime() {
      return zonedDateTime;
    }

    public DateTimeHolder setZonedDateTime(ZonedDateTime zonedDateTime) {
      this.zonedDateTime = zonedDateTime;
      return this;
    }

    public ZonedDateTime getZonedDateTimeWithMillis() {
      return zonedDateTimeWithMillis;
    }

    public DateTimeHolder setZonedDateTimeWithMillis(ZonedDateTime zonedDateTimeWithMillis) {
      this.zonedDateTimeWithMillis = zonedDateTimeWithMillis;
      return this;
    }
  }
}
