/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mongodb.converter;

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
