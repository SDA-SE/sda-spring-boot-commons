/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MetadataContextClientTestController {

  private final MetadataOtherServiceClient metadataOtherServiceClient;

  private final MetadataOtherServiceAuthenticatedClient metadataOtherServiceAuthenticatedClient;

  public MetadataContextClientTestController(
      MetadataOtherServiceClient metadataOtherServiceClient,
      MetadataOtherServiceAuthenticatedClient metadataOtherServiceAuthenticatedClient) {
    this.metadataOtherServiceClient = metadataOtherServiceClient;
    this.metadataOtherServiceAuthenticatedClient = metadataOtherServiceAuthenticatedClient;
  }

  @GetMapping("/metadataProxy")
  public Object getSomethingFromOtherService() {
    return metadataOtherServiceClient.getSomething();
  }

  @GetMapping("/metadataAuthProxy")
  public Object getSomethingFromOtherServiceWithAuthentication() {
    return metadataOtherServiceAuthenticatedClient.getSomething();
  }
}
