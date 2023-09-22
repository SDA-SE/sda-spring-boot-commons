/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client;

import java.util.List;
import org.springframework.web.service.annotation.GetExchange;
import reactor.core.publisher.Mono;

/**
 * WebClient example using an interface and annotations. It will be instantiated in the {@link
 * SdaClientConfiguration} class, using a factory.
 */
interface EmployeeClient {

  /**
   * We can also have @HttpExchange, @PostExchange, @PutExchange, @PatchExchange
   * and @DeleteExchange. We can return Mono<Void> if we want to return nothing.
   *
   * @return a reactive stream containing a list of employees
   */
  @GetExchange("/employees")
  Mono<List<Employee>> getAll();
}
