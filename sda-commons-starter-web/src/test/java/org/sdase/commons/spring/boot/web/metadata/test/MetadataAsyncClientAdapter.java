/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.concurrent.CompletableFuture;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class MetadataAsyncClientAdapter {

  private final ObjectMapper objectMapper;

  private final MetadataOtherServiceAuthenticatedClient otherServiceAuthenticatedClient;

  private final MetadataPlatformServiceClient metadataPlatformServiceClient;

  public MetadataAsyncClientAdapter(
      ObjectMapper objectMapper,
      MetadataOtherServiceAuthenticatedClient otherServiceAuthenticatedClient,
      MetadataPlatformServiceClient metadataPlatformServiceClient) {
    this.objectMapper = objectMapper;
    this.otherServiceAuthenticatedClient = otherServiceAuthenticatedClient;
    this.metadataPlatformServiceClient = metadataPlatformServiceClient;
  }

  @Async
  public CompletableFuture<ObjectNode> getSomethingAsync() {
    var something = otherServiceAuthenticatedClient.getSomething();
    var jsonNodes = objectMapper.convertValue(something, ObjectNode.class);
    var time = System.nanoTime();
    var timeAsJsonNode = objectMapper.convertValue(time, JsonNode.class);
    jsonNodes.set("time", timeAsJsonNode);
    return CompletableFuture.completedFuture(jsonNodes);
  }

  @Async
  public CompletableFuture<ObjectNode> getPlatformSomethingAsync() {
    var something = metadataPlatformServiceClient.getSomething();
    var jsonNodes = objectMapper.convertValue(something, ObjectNode.class);
    var time = System.nanoTime();
    var timeAsJsonNode = objectMapper.convertValue(time, JsonNode.class);
    jsonNodes.set("time", timeAsJsonNode);
    return CompletableFuture.completedFuture(jsonNodes);
  }
}
