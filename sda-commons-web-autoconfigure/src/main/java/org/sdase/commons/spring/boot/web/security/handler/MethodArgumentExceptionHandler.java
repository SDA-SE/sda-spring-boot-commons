/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.handler;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import org.sdase.commons.spring.boot.web.error.ApiError;
import org.sdase.commons.spring.boot.web.error.ApiInvalidParam;
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
    // changing the input so that the result matches the way Guava (used before) created camel case
    String normalizedToMatchGuava = camelCase;
    boolean allNormalized = false;
    while (!allNormalized) {
      String newNormalized = normalizeToMatchGuava(normalizedToMatchGuava);
      allNormalized = newNormalized.equals(normalizedToMatchGuava);
      normalizedToMatchGuava = newNormalized;
    }
    // end of backward compatibility implementation to match Guava transformation
    return ERROR_CODE_TRANSLATOR.translate(normalizedToMatchGuava);
  }

  private static String normalizeToMatchGuava(String normalizedToMatchGuava) {
    return normalizedToMatchGuava.replaceAll("([A-Z])([A-Z])", "$1_$2");
  }
}
