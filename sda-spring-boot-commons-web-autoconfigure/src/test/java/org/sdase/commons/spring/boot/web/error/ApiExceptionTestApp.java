/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.error;

import org.sdase.commons.spring.boot.web.error.ApiException.FinalBuilder;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class ApiExceptionTestApp {

  @PostMapping("/throw")
  public ResponseEntity<?> throwApiException(@RequestBody ApiError apiError) {
    ApiInvalidParam apiInvalidParam =
        apiError.getInvalidParams().isEmpty() ? null : apiError.getInvalidParams().get(0);
    FinalBuilder builder =
        ApiException.builder()
            .httpCode(Integer.parseInt(apiError.getTitle()))
            .title(apiError.getTitle());
    if (apiInvalidParam != null) {
      builder.detail(
          apiInvalidParam.getField(), apiInvalidParam.getReason(), apiInvalidParam.getErrorCode());
    }
    throw builder.build();
  }
}
