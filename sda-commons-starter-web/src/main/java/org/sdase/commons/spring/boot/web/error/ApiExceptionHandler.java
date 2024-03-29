/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.error;

import jakarta.servlet.http.HttpServletRequest;
import org.sdase.commons.spring.boot.error.ApiError;
import org.sdase.commons.spring.boot.error.ApiException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

  @ResponseBody
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiError> handleApiException(HttpServletRequest request, ApiException ex) {
    HttpStatus statusCode = HttpStatus.resolve(ex.getHttpCode());
    return new ResponseEntity<>(
        ex.getDTO(), statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
