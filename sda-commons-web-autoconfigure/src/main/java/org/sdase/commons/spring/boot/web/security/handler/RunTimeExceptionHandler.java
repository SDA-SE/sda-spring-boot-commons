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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
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

  private static final PropertyNamingStrategies.UpperSnakeCaseStrategy ERROR_CODE_TRANSLATOR =
      new PropertyNamingStrategies.UpperSnakeCaseStrategy();

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> validationError(MethodArgumentNotValidException ex) {
    var response =
        new ApiError(
            "Validation error",
            ex.getBindingResult().getFieldErrors().stream()
                .map(
                    fieldError ->
                        new ApiInvalidParam(
                            fieldError.getField(),
                            fieldError.getDefaultMessage(),
                            camelToUpperSnakeCase(fieldError.getCode())))
                .toList());
    return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
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
