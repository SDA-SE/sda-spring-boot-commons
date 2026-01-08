/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client.test;

import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@Component
public class AsyncClientAdapter {

  private final ObjectMapper objectMapper;

  private final OtherServiceAuthenticatedClient otherServiceAuthenticatedClient;

  private final ClientTestConstraints clientTestConstraints;

  public AsyncClientAdapter(
      ObjectMapper objectMapper,
      OtherServiceAuthenticatedClient otherServiceAuthenticatedClient,
      ClientTestConstraints clientTestConstraints) {
    this.objectMapper = objectMapper;
    this.otherServiceAuthenticatedClient = otherServiceAuthenticatedClient;
    this.clientTestConstraints = clientTestConstraints;
  }

  @Async
  CompletableFuture<Object> getSomethingAsync() {
    if (!clientTestConstraints.isCallAsyncAllowed()) {
      throw new IllegalStateException("Calling async is not allowed.");
    }
    var something = otherServiceAuthenticatedClient.getSomething();
    var jsonNodes = objectMapper.convertValue(something, ObjectNode.class);
    var time = System.nanoTime();
    var timeAsJsonNode = objectMapper.convertValue(time, JsonNode.class);
    jsonNodes.set("time", timeAsJsonNode);
    return CompletableFuture.completedFuture(objectMapper.convertValue(jsonNodes, Object.class));
  }
}
