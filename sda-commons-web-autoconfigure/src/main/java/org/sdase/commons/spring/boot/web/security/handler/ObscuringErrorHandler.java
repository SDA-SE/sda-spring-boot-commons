/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.handler;

import org.sdase.commons.spring.boot.web.error.ApiError;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Error handle that replaces default errors responses with custom {@link ApiError}.
 *
 * <p>This handler is invoked for explicitly defined error responses like {@code return
 * ResponseEntity.status(404).build();} and is not invoked for response indirectly create by {@code
 * throw new NotFoundException()}.
 *
 * <p>This handler addresses risks identified in the security guide as:
 *
 * <ul>
 *   <li>"Risk: Detection of confidential components ... Removal of application related Error
 *       messages."
 * </ul>
 */
@RestControllerAdvice
public class ObscuringErrorHandler implements ResponseBodyAdvice<Object> {
  private static final String RESPONSE_ENTITY_TYPE = ResponseEntity.class.getName();
  private static final String ACTUATOR_PACKAGE_NAME =
      "org.springframework.boot.actuate.endpoint.web.servlet";
  private static final String API_ERROR_RESPONSE_ENTITY_TYPE =
      String.format("%s<%s>", ResponseEntity.class.getTypeName(), ApiError.class.getTypeName());

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
    return shouldTransformResponse(returnType);
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    var responseStatus =
        HttpStatus.resolve(((ServletServerHttpResponse) response).getServletResponse().getStatus());

    // No replacement for `ResponseEntity.status(204).build()`
    if (responseStatus == null || !responseStatus.isError()) {
      return body;
    }

    // Replacement error responses with standard errors, e.g
    // `ResponseEntity.internalServerError().body(customErrorBody)
    var apiError = new ApiError();
    apiError.setTitle("HTTP Error " + responseStatus.value() + " occurred.");
    return apiError;
  }

  private boolean shouldTransformResponse(MethodParameter returnType) {
    // No replacement for `ResponseEntity<ApiError>`
    if (API_ERROR_RESPONSE_ENTITY_TYPE.equals(returnType.getGenericParameterType().getTypeName())) {
      return false;
    }

    // No replacement for `ResponseEntity<MyDto>`
    if (!RESPONSE_ENTITY_TYPE.equals(returnType.getParameterType().getTypeName())) {
      return false;
    }

    // No replacement for `ResponseEntity<Object>` returned by spring actuator
    return !ACTUATOR_PACKAGE_NAME.equals(
        returnType.getExecutable().getDeclaringClass().getPackageName());
  }
}
