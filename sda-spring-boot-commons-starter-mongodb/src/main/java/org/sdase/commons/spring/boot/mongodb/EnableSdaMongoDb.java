/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.mongodb;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.sdase.commons.spring.boot.mongodb.converter.SdaMongoCustomConverterConfiguration;
import org.springframework.context.annotation.Import;

/**
 * Enables features that make a Spring Boot service using MongoDB ready to use within the SDA
 * platform.
 *
 * <p>So far this covers:
 *
 * <ul>
 *   <li>{@linkplain org.springframework.core.convert.converter.Converter Converter} for {@linkplain
 *       java.time.ZonedDateTime}
 *   <li>Enable auto-index creation for {@linkplain
 *       org.springframework.data.mongodb.core.index.Indexed @Indexed}-Annotation
 * </ul>
 *
 * <p>Not covered:
 *
 * <ul>
 *   <li>Mongo Client Options are only configurable via connection uri
 *   <li>The health check will be combined with the actuator
 *   <li>CA certificate can't be configured via environment variable
 *   <li>Traced connection to MongoDB server
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import(
    value = {
      SdaMongoDbConfiguration.class,
      SdaMongoCustomConverterConfiguration.class,
    })
public @interface EnableSdaMongoDb {}
