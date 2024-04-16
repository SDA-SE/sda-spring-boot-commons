/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.logging;

import static ch.qos.logback.core.CoreConstants.UTF_8_CHARSET;

import ch.qos.logback.classic.encoder.JsonEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class SdaLoggingJsonEncoder extends JsonEncoder {

  @Override
  public byte[] encode(ILoggingEvent event) {
    byte[] logLine = super.encode(event);

    String replacedThreadName = new String(logLine).replaceFirst("threadName", "thread");
    String replacedLogger = replacedThreadName.replaceFirst("loggerName", "logger");

    return replacedLogger.getBytes(UTF_8_CHARSET);
  }
}
