/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.client.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Component;

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
    return new AsyncResult<>(objectMapper.convertValue(jsonNodes, Object.class)).completable();
  }
}
