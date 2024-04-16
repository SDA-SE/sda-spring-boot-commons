/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.logging;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.slf4j.helpers.BasicMDCAdapter;

class SdaLoggingJsonEncoderTest {

  private SdaLoggingJsonEncoder encoder = new SdaLoggingJsonEncoder();

  @Test
  void shouldEncodeMessage() {

    LoggingEvent loggingEvent = new LoggingEvent();
    LoggerContext loggerContext = new LoggerContext();
    loggerContext.setMDCAdapter(new BasicMDCAdapter());
    loggingEvent.setLoggerContext(loggerContext);
    loggingEvent.setTimeStamp(123456789L);
    loggingEvent.setLevel(Level.INFO);
    loggingEvent.setMessage("message");
    loggingEvent.setThreadName("threadName");
    loggingEvent.setLoggerName("loggerName");
    loggingEvent.setMDCPropertyMap(Collections.singletonMap("keyMDC", "valueMDC"));

    byte[] encode = encoder.encode(loggingEvent);

    String expected =
        "{\"timestamp\":123456789,\"level\":\"INFO\",\"thread\":\"threadName\",\"logger\":\"loggerName\",\"context\":null,\"mdc\": {\"keyMDC\":\"valueMDC\"},\"message\":\"message\",\"throwable\":null}\n";
    assertThat(new String(encode)).isEqualTo(expected);
  }
}
