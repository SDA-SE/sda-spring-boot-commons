/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.jackson;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import tools.jackson.databind.jsonFormatVisitors.JsonValueFormat;
import tools.jackson.databind.ser.std.StdSerializer;

/**
 * A serializer used to write {@link ZonedDateTime} in ISO 8601 datetime format contain date, hours,
 * minutes, seconds and time zone.
 *
 * <p><strong> The serializer should be used instead of {@link JsonFormat#pattern()} because it does
 * not affect the tolerant deserialization Jackson provides by default. </strong>
 *
 * <p>Use the serializer at {@code ZonedDateTime} properties to activate it:
 *
 * <pre>
 *   class MyResource {
 *     &#64;JsonSerialize(using = Iso8601Serializer.class)
 *     private ZonedDateTime createdAt;
 *     // writes json as {"createdAt": "2018-11-21T13:16:47Z"} for UTC
 *     // or {"createdAt": "2018-11-21T13:16:47+01:00"} for CET
 *     // ...
 *   }
 * }</pre>
 *
 * <p>Note that there is a subclass to write including milliseconds: {@link WithMillis}
 */
public class Iso8601Serializer extends StdSerializer<ZonedDateTime> {

  private final transient DateTimeFormatter formatter;

  // used by Jackson
  @SuppressWarnings("WeakerAccess")
  public Iso8601Serializer() {
    super(ZonedDateTime.class);
    this.formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");
  }

  /**
   * Constructor for subclasses that create a new {@link Iso8601Serializer} for {@link
   * ZonedDateTime} using a custom pattern for formatting.
   *
   * @param pattern the pattern used for formatting, see {@link DateTimeFormatter#ofPattern(String)}
   */
  @SuppressWarnings("WeakerAccess")
  protected Iso8601Serializer(String pattern) {
    super(ZonedDateTime.class);
    this.formatter = DateTimeFormatter.ofPattern(pattern);
  }

  @Override
  public void serialize(ZonedDateTime value, JsonGenerator gen, SerializationContext provider)
      throws JacksonException {
    gen.writeString(formatter.format(value));
  }

  @Override
  public void acceptJsonFormatVisitor(JsonFormatVisitorWrapper visitor, JavaType typeHint) {
    visitStringFormat(visitor, typeHint, JsonValueFormat.DATE_TIME);
  }

  /**
   * A serializer used to write {@link ZonedDateTime} in ISO 8601 datetime format contain date,
   * hours, minutes, seconds, milliseconds and time zone.
   *
   * <p><strong> The serializer should be used instead of {@link JsonFormat#pattern()} because it
   * does not affect the tolerant deserialization Jackson provides by default.</strong>
   *
   * <p>Use the serializer at {@code ZonedDateTime} properties to activate it:
   *
   * <pre>
   *   class MyResource {
   *     &#64;JsonSerialize(using = Iso8601Serializer.WithMillis.class)
   *     private ZonedDateTime createdAt;
   *     // writes json as {"createdAt": "2018-11-21T13:16:47.123Z"} for UTC
   *     // or {"createdAt": "2018-11-21T13:16:47.123+01:00"} for CET
   *     // ...
   *   }
   * }</pre>
   */
  public static class WithMillis extends Iso8601Serializer {

    // for Jackson
    @SuppressWarnings("WeakerAccess")
    public WithMillis() {
      super("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
    }
  }

  /**
   * A serializer used to write {@link ZonedDateTime} in ISO 8601 datetime format contain date,
   * hours, minutes, seconds, nanoseconds and time zone.
   *
   * <p><strong> The serializer should be used instead of {@link JsonFormat#pattern()} because it
   * does not affect the tolerant deserialization Jackson provides by default.</strong>
   *
   * <p>Use the serializer at {@code ZonedDateTime} properties to activate it:
   *
   * <pre>
   *   class MyResource {
   *     &#64;JsonSerialize(using = Iso8601Serializer.WithNanos.class)
   *     private ZonedDateTime createdAt;
   *     // writes json as {"createdAt": "2018-11-21T13:16:47.123456789Z"} for UTC
   *     // or {"createdAt": "2018-11-21T13:16:47.1234567+01:00"} for CET
   *     // ...
   *   }
   * }</pre>
   */
  public static class WithNanos extends Iso8601Serializer {

    // for Jackson
    @SuppressWarnings("WeakerAccess")
    public WithNanos() {
      super("yyyy-MM-dd'T'HH:mm:ss.nnnnnnnnnXXX");
    }
  }
}
