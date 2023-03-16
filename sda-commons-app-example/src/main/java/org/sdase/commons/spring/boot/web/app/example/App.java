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
import java.util.List;
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

  @GetMapping("trees")
  @ApiResponse(
      description = "Resource not found",
      content = @Content(schema = @Schema(implementation = ApiError.class)),
      responseCode = "404")
  @ApiResponse(
      description = "A successful response",
      content = @Content(schema = @Schema(implementation = Trees.class)),
      responseCode = "200")
  public Trees getTrees() {
    return new Trees()
        .setTrees(List.of(new Tree().setName("Yellow Birch"), new Tree().setName("American Elm")));
  }

  @GetMapping("cars")
  @ApiResponse(
      description = "Resource not found",
      content = @Content(schema = @Schema(implementation = ApiError.class)),
      responseCode = "404")
  @ApiResponse(
      description = "A successful response",
      content = @Content(schema = @Schema(implementation = Cars.class)),
      responseCode = "200")
  public Cars getCars() {
    return new Cars()
        .setCars(
            List.of(
                new Car().setLicensePlate("HH-AB 1200"), new Car().setLicensePlate("LG-CD 2000")));
  }
}
