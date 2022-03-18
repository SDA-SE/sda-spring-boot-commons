/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.jackson.test;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.sdase.commons.spring.boot.starter.EnableSdaPlatform;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@EnableSdaPlatform
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
