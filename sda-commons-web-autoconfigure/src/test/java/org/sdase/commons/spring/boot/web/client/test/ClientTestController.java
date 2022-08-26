/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ClientTestController {

  private final OtherServiceClient otherServiceClient;

  private final OtherServiceAuthenticatedClient otherServiceAuthenticatedClient;

  private final AsyncClientAdapter asyncClientAdapter;

  public ClientTestController(
      OtherServiceClient otherServiceClient,
      OtherServiceAuthenticatedClient otherServiceAuthenticatedClient,
      AsyncClientAdapter asyncClientAdapter) {
    this.otherServiceClient = otherServiceClient;
    this.otherServiceAuthenticatedClient = otherServiceAuthenticatedClient;
    this.asyncClientAdapter = asyncClientAdapter;
  }

  @GetMapping("/proxy")
  public Object getSomethingFromOtherService() {
    return otherServiceClient.getSomething();
  }

  @GetMapping("/authProxy")
  public Object getSomethingFromOtherServiceWithAuthentication() {
    return otherServiceAuthenticatedClient.getSomething();
  }

  @GetMapping("/authProxyAsync")
  public Object getSomethingFromOtherServiceWithAuthenticationAsync() {
    List<CompletableFuture<Object>> responses = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      responses.add(asyncClientAdapter.getSomethingAsync());
    }
    return responses.stream().map(CompletableFuture::join).collect(Collectors.toList());
  }
}
