/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SecurityTestApp {

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

  @RequestMapping(value = "/traced", method = RequestMethod.TRACE)
  public String serveTrace() {
    return "This should not be allowed";
  }

  @GetMapping(value = "/error")
  public Object throwError() throws IOException {
    throw new IOException("This should not be allowed");
  }

  @GetMapping(value = "/errorEntity")
  public ResponseEntity<?> responseError() {
    var body =
        ResponseEntity.internalServerError()
            .body(new TestResource().setValue("This should not be leaked."));
    return body;
  }

  @GetMapping(value = "/response")
  public ResponseEntity<?> responseEntity() {
    return new ResponseEntity<>(
        new TestResource().setValue("This will not be altered."), HttpStatus.CREATED);
  }

  @GetMapping(value = "/resource")
  public TestResource resource() {
    return new TestResource().setValue("This will not be altered.");
  }
}
