/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.tracing.app;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class KafkaTraceTestRestController {

  @Autowired KafkaTraceTestProducer kafkaTraceTestProducer;

  @GetMapping("/createEvent")
  public String createEventFromRestCall(@RequestParam("user") String user)
      throws ExecutionException, InterruptedException, TimeoutException {

    kafkaTraceTestProducer.send(user);
    return "User Name: " + user;
  }
}
