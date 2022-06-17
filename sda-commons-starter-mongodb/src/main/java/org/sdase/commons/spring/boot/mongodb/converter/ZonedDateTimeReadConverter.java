/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.mongodb.converter;

import static java.time.ZoneOffset.UTC;

import java.time.ZonedDateTime;
import java.util.Date;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;

@ReadingConverter
public enum ZonedDateTimeReadConverter implements Converter<Object, ZonedDateTime> {
  INSTANCE;

  @Override
  public ZonedDateTime convert(Object fromDBObject) {
    if (fromDBObject instanceof Date date) {
      return ZonedDateTime.ofInstant(date.toInstant(), UTC);
    }

    if (fromDBObject instanceof String stringDate) {
      return ZonedDateTime.ofInstant(ZonedDateTime.parse(stringDate).toInstant(), UTC);
    }

    throw new IllegalArgumentException("Can't convert to ZonedDateTime from " + fromDBObject);
  }
}
