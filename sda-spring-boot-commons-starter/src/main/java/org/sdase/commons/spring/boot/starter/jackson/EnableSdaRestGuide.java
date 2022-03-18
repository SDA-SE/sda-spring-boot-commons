/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.jackson;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * Enables features that make a Spring Boot service compliant with the <a
 * href="https://sda.dev/core-concepts/communication/restful-api-guide/">SDA SE RESTful API
 * Guide</a>.
 *
 * <p>So far this covers:
 *
 * <ul>
 *   <li>the tolerant reader pattern
 *   <li>consistent serialization of {@link java.time.ZonedDateTime} compatible to the <a
 *       href="https://json-schema.org/understanding-json-schema/reference/string.html#dates-and-times">{@code
 *       type} {@code date-time} of JSON-Schema</a>.
 *       <p>It is strongly recommended to use
 *       <ul>
 *         <li>{@link java.time.LocalDate} for dates without time serialized as {@code 2018-09-23}
 *         <li>{@link java.time.ZonedDateTime} for date and times serialized as {@code
 *             2018-09-23T14:21:41+01:00}
 *         <li>{@link java.time.Duration} for durations with time resolution serialized as {@code
 *             P1DT13M}
 *         <li>{@link java.time.Period} for durations with day resolution serialized as {@code
 *             P1Y2D}
 *       </ul>
 *       <p>All these types can be read and written in JSON as ISO 8601 formats.
 *       <p>Reading {@link java.time.ZonedDateTime} is configured to be tolerant so that added
 *       nanoseconds or missing milliseconds or missing seconds are supported.
 *       <p>{@code @}{@link com.fasterxml.jackson.annotation.JsonFormat}{@code (pattern = "...")}
 *       <strong>should not be used</strong> for customizing serialization because it breaks
 *       tolerant reading of formatting variants. If a specific field should be serialized with
 *       milliseconds, it must be annotated with {@code @}{@link
 *       com.fasterxml.jackson.databind.annotation.JsonSerialize}{@code (using = }{@link
 *       Iso8601Serializer.WithMillis}{@code .class)}. If a specific field should be serialized with
 *       nanoseconds, it must be annotated with {@code @}{@link
 *       com.fasterxml.jackson.databind.annotation.JsonSerialize}{@code (using = }{@link
 *       Iso8601Serializer.WithNanos}{@code .class)}.
 * </ul>
 *
 * <p><strong>Differences to the known <a
 * href="https://github.com/SDA-SE/sda-dropwizard-commons/tree/master/sda-commons-server-jackson">SDA
 * Dropwizard Commons configuration</a></strong>
 *
 * <ul>
 *   <li><strong>{@link java.time.ZonedDateTime}</strong> fields are serialized with seconds by
 *       default. There is no other global configuration for {@link java.time.ZonedDateTime}
 *       serialization available.
 *   <li><strong>Less modules are activated for foreign frameworks</strong>. Compared to SDA
 *       Dropwizard Commons, {@code GuavaExtrasModule}, {@code JodaModule}, {@code
 *       AfterburnerModule} and {@code CaffeineModule} are not registered any more.
 *   <li><strong>No documented customization of the global {@link
 *       com.fasterxml.jackson.databind.ObjectMapper}</strong> is available right now.
 *   <li>Support for <strong>HAL Links and embedding linked resources</strong> is not implemented
 *       yet.
 *   <li>Support for <strong>YAML</strong> is not implemented yet.
 *   <li>The <a
 *       href="https://sda.dev/core-concepts/communication/restful-api-guide/#RESTfulAPIGuide-MUST%3AUseerrorJSON">SDA
 *       SE error model</a> is not provided yet.
 *   <li>There is <strong>no support for <a
 *       href="https://sda.dev/core-concepts/communication/restful-api-guide/#RESTfulAPIGuide-MAY%3AProvidefieldfilteringtoretrievepartialresources">field
 *       filters</a></strong>. Such filters have been barely used in the SDA SE.
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({SdaObjectMapperConfiguration.class})
public @interface EnableSdaRestGuide {
  // TODO provide the missing features of the rest guide as documented above
}
