/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.handler.app;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ValidationExceptionRestController {

  @PostMapping("/bookSpring")
  public ValidationExceptionBook getBookSpring(
      @Validated @RequestBody @NotNull ValidationExceptionBook book) {
    return new ValidationExceptionBook(book.title(), book.author());
  }

  @PostMapping("/bookJakarta")
  public ValidationExceptionBook getBookJakarta(
      @Valid @RequestBody @NotNull ValidationExceptionBook book) {
    return new ValidationExceptionBook(book.title(), book.author());
  }

  @PostMapping("/bookString")
  public String getBookNull(@Validated @RequestBody @Size(min = 3) String book) {
    return book;
  }
}
