/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.test;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import org.sdase.commons.spring.boot.web.error.ApiException;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@SpringBootApplication
@RestController
public class SecurityTestApp {

  @RequestMapping(value = "/traced", method = RequestMethod.TRACE)
  public String serveTrace() {
    return "This should not be allowed";
  }

  @GetMapping(value = "/forcedError")
  public Object throwError() {
    throw new RuntimeException("This should not be allowed");
  }

  @GetMapping(value = "/apiError")
  public void throwApiError() {
    throw ApiException.builder()
        .httpCode(HttpStatus.NOT_IMPLEMENTED.value())
        .title("This method is not implemented yet.")
        .build();
  }

  @GetMapping(value = "/errorEntity")
  public ResponseEntity<?> responseError() {
    return ResponseEntity.internalServerError()
        .body(new TestResource().setValue("This should not be leaked."));
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

  @GetMapping(value = "/header")
  public ResponseEntity<TestResource> header(
      @RequestParam("headerName") String headerName,
      @RequestParam("headerValue") String headerValue) {
    var headers = new HttpHeaders();
    headers.add(headerName, headerValue);
    return new ResponseEntity<>(new TestResource(), headers, HttpStatus.OK);
  }

  @GetMapping(value = "/voidError")
  public ResponseEntity<Void> voidError() {
    return ResponseEntity.notFound().build();
  }

  @GetMapping(value = "caller", produces = "text/plain")
  public String identifyCaller(@Context HttpServletRequest request) {
    return request.getRemoteAddr();
  }

  @GetMapping(value = "link", produces = "text/plain")
  public String identifyLink(@Context HttpServletRequest request) {
    return ServletUriComponentsBuilder.fromCurrentContextPath().toUriString();
  }
}
