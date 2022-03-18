/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.mongodb.starter.converter;

import java.time.ZonedDateTime;
import java.util.Date;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public enum ZonedDateTimeWriteConverter implements Converter<ZonedDateTime, Date> {
  INSTANCE;

  @Override
  public Date convert(ZonedDateTime source) {
    return Date.from(source.toInstant());
  }
}
