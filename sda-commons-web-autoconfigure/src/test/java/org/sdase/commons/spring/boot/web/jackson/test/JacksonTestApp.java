/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.jackson.test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class JacksonTestApp {

  @GetMapping("/fixedTime")
  public Object getFixedTime() {
    return new Object() {
      private final ZonedDateTime time =
          // the configured ObjectMapper should truncate the nanos to seconds
          ZonedDateTime.of(LocalDateTime.of(2018, 11, 21, 13, 16, 47, 965_000_300), ZoneOffset.UTC);

      @SuppressWarnings("unused")
      public ZonedDateTime getTime() {
        return time;
      }
    };
  }
}
