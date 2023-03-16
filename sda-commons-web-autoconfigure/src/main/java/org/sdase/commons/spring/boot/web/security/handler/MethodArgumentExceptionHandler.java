/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.handler;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.sdase.commons.spring.boot.error.ApiError;
import org.sdase.commons.spring.boot.error.ApiInvalidParam;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * This handler addresses risks identified in the security guide as:
 *
 * <ul>
 *   <li>"Risk: Detection of confidential components ... Removal of application related Error
 *       messages."
 * </ul>
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MethodArgumentExceptionHandler {
  private static final String ERROR_MESSAGE = "Validation error";

  private static final PropertyNamingStrategies.UpperSnakeCaseStrategy ERROR_CODE_TRANSLATOR =
      new PropertyNamingStrategies.UpperSnakeCaseStrategy();

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @Order(value = Ordered.HIGHEST_PRECEDENCE)
  public ResponseEntity<ApiError> validationError(MethodArgumentNotValidException ex) {
    var response =
        new ApiError(
            ERROR_MESSAGE,
            ex.getBindingResult().getFieldErrors().stream()
                .map(
                    fieldError ->
                        new ApiInvalidParam(
                            fieldError.getField(),
                            fieldError.getDefaultMessage(),
                            camelToUpperSnakeCase(fieldError.getCode())))
                .toList());
    return ResponseEntity.unprocessableEntity().body(response);
  }

  static String camelToUpperSnakeCase(String camelCase) {
    return ERROR_CODE_TRANSLATOR.translate(camelCase);
  }
}
