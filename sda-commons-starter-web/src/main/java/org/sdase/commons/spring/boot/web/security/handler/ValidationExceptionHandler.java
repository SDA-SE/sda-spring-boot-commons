/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.handler;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import java.util.List;
import org.sdase.commons.spring.boot.error.ApiError;
import org.sdase.commons.spring.boot.error.ApiInvalidParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * This handler addresses risks identified in the security guide as:
 *
 * <ul>
 *   <li>"Risk: Detection of confidential components ... Removal of application related Error
 *       messages."
 * </ul>
 */
@RestControllerAdvice()
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(
    prefix = "security",
    name = "validation-exception-handler-enabled",
    havingValue = "true",
    matchIfMissing = true)
public class ValidationExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(ValidationExceptionHandler.class);

  public static final String ERROR_MESSAGE = "Validation error";
  private static final String MESSAGE_NOT_READABLE = "Request Body not readable";

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

    LOG.error(ex.getMessage(), ex);
    return ResponseEntity.unprocessableEntity().body(response);
  }

  @ExceptionHandler({HandlerMethodValidationException.class})
  public ResponseEntity<ApiError> validationErrorJakartaAnnotation(
      HandlerMethodValidationException ex) {

    ApiError apiError = new ApiError(ERROR_MESSAGE);

    //    If MessageSourceResolvable is instance of FieldError, then add additional information.
    List<ApiInvalidParam> invalidParams =
        ex.getAllValidationResults().stream()
            .flatMap(
                parameterValidationResult ->
                    parameterValidationResult.getResolvableErrors().stream())
            .filter(FieldError.class::isInstance)
            .map(FieldError.class::cast)
            .map(
                fieldError ->
                    new ApiInvalidParam(
                        fieldError.getField(),
                        fieldError.getDefaultMessage(),
                        camelToUpperSnakeCase(fieldError.getCode())))
            .toList();
    apiError.addInvalidParams(invalidParams);

    LOG.error(ex.getMessage(), ex);
    return ResponseEntity.unprocessableEntity().body(apiError);
  }

  @ExceptionHandler({HttpMessageNotReadableException.class})
  public ResponseEntity<ApiError> validationErrorResponseBodyNull(
      HttpMessageNotReadableException ex) {

    LOG.error(ex.getMessage(), ex);
    return ResponseEntity.unprocessableEntity().body(new ApiError(MESSAGE_NOT_READABLE));
  }

  static String camelToUpperSnakeCase(String camelCase) {
    return ERROR_CODE_TRANSLATOR.translate(camelCase);
  }
}
