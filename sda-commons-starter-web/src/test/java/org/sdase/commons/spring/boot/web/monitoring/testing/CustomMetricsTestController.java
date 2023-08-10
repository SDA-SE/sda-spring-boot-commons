/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.monitoring.testing;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Random;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CustomMetricsTestController {

  private final Counter someOperationSuccessCounter;

  private final Counter someOperationErrorCounter;

  private final Random random = new Random();

  public CustomMetricsTestController(MeterRegistry registry) {
    someOperationSuccessCounter =
        registry.counter(
            "some_operation_success_counter_total",
            "some_operation_error_counter_total",
            "Counts successes occurred when some operation is invoked.");

    // Create a Counter to count events.
    someOperationErrorCounter =
        registry.counter(
            "some_operation_error_counter_total",
            "some_operation_error_counter_total",
            "Counts errors occurred when some operation is invoked.");
  }

  @GetMapping("/add-custom-metrics")
  public String index() {
    for (int i = 0; i < 10; i++) {
      doSomeOperationWithCounting();
    }
    return "Hello World!";
  }

  public void doSomeOperationWithCounting() {

    // do some business logic here and realize if it is successful or not
    boolean success = random.nextBoolean();

    // track the success by incrementing the counter
    if (success) {
      someOperationSuccessCounter.increment();
    } else {
      someOperationErrorCounter.increment();
    }
  }
}
