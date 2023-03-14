/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.sdase.commons.spring.boot.web.error.ApiError;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class App {

  public static void main(String[] args) {
    SpringApplication.run(App.class, args);
  }

  @GetMapping("myResource")
  @ApiResponse(
      description = "Resource not found",
      content = @Content(schema = @Schema(implementation = ApiError.class)),
      responseCode = "404")
  @ApiResponse(
      description = "A successful response",
      content = @Content(schema = @Schema(implementation = MyResource.class)),
      responseCode = "200")
  public MyResource getMyResource() {
    return new MyResource().setValue("the value");
  }
}
