/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.handler;

import org.sdase.commons.spring.boot.error.ApiError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * This handler addresses risks identified in the security guide as:
 *
 * <ul>
 *   <li>"Risk: Detection of confidential components ... Removal of application related Error
 *       messages."
 * </ul>
 */
@ControllerAdvice
public class RunTimeExceptionHandler extends ResponseEntityExceptionHandler {
  private static final String ERROR_MESSAGE = "An exception occurred.";
  private static final Logger LOG = LoggerFactory.getLogger(RunTimeExceptionHandler.class);

  @ExceptionHandler(value = {RuntimeException.class})
  protected ResponseEntity<ApiError> handleRuntimeException(
      RuntimeException ex, WebRequest request) {
    LOG.error(ERROR_MESSAGE, ex);
    var headers = new HttpHeaders();
    return new ResponseEntity<>(
        new ApiError(ERROR_MESSAGE), headers, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
