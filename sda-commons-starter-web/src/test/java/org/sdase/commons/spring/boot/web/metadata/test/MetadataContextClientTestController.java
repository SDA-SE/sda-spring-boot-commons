/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.http.converter.autoconfigure.ClientHttpMessageConvertersCustomizer;
import org.springframework.cloud.openfeign.support.FeignHttpMessageConverters;
import org.springframework.cloud.openfeign.support.HttpMessageConverterCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.node.ObjectNode;

@RestController
public class MetadataContextClientTestController {

  private final MetadataOtherServiceClient metadataOtherServiceClient;

  private final MetadataOtherServiceAuthenticatedClient metadataOtherServiceAuthenticatedClient;

  private final MetadataPlatformServiceClient metadataPlatformServiceClient;

  private final MetadataAsyncClientAdapter asyncClientAdapter;

  public MetadataContextClientTestController(
      MetadataOtherServiceClient metadataOtherServiceClient,
      MetadataOtherServiceAuthenticatedClient metadataOtherServiceAuthenticatedClient,
      MetadataAsyncClientAdapter asyncClientAdapter,
      MetadataPlatformServiceClient metadataPlatformServiceClient) {
    this.metadataOtherServiceClient = metadataOtherServiceClient;
    this.metadataOtherServiceAuthenticatedClient = metadataOtherServiceAuthenticatedClient;
    this.asyncClientAdapter = asyncClientAdapter;
    this.metadataPlatformServiceClient = metadataPlatformServiceClient;
  }

  @GetMapping("/metadataProxy")
  public Object getSomethingFromOtherService() {
    return metadataOtherServiceClient.getSomething();
  }

  @GetMapping("/metadataAuthProxy")
  public Object getSomethingFromOtherServiceWithAuthentication() {
    return metadataOtherServiceAuthenticatedClient.getSomething();
  }

  @GetMapping("/metadataAuthProxyAsync")
  public Object getSomethingFromOtherServiceAsyncWithAuthentication() {
    List<CompletableFuture<ObjectNode>> responses = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      responses.add(asyncClientAdapter.getSomethingAsync());
    }
    return responses.stream().map(CompletableFuture::join).toList();
  }

  @GetMapping("/metadataPlatformProxy")
  public Object getSomethingFromPlatformService() {
    return metadataPlatformServiceClient.getSomething();
  }

  @GetMapping("/metadataPlatformProxyAsync")
  public Object getSomethingFromPlatformServiceAsync() {
    List<CompletableFuture<ObjectNode>> responses = new ArrayList<>();
    for (int i = 0; i < 100; i++) {
      responses.add(asyncClientAdapter.getPlatformSomethingAsync());
    }
    return responses.stream().map(CompletableFuture::join).toList();
  }

  @Bean
  @ConditionalOnMissingBean
  // see https://github.com/spring-cloud/spring-cloud-openfeign/issues/1307
  public FeignHttpMessageConverters feignHttpMessageConverters(
      ObjectProvider<ClientHttpMessageConvertersCustomizer> messageConverters,
      ObjectProvider<HttpMessageConverterCustomizer> customizers) {
    var feignHttpMessageConverters = new FeignHttpMessageConverters(messageConverters, customizers);
    // init converters
    feignHttpMessageConverters.getConverters();
    return feignHttpMessageConverters;
  }
}
