package org.sdase.commons.spring.boot.web.error;

import javax.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

  @ResponseBody
  @ExceptionHandler(ApiException.class)
  public ResponseEntity<ApiError> handleApiException(HttpServletRequest request, ApiException ex) {
    HttpStatus statusCode = HttpStatus.resolve(ex.getHttpCode());
    return new ResponseEntity<>(
        ex.getDTO(), statusCode != null ? statusCode : HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
