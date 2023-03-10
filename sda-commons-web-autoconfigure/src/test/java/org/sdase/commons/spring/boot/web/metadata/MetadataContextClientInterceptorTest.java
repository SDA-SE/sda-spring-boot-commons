/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import feign.RequestTemplate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sdase.commons.spring.boot.metadata.context.DetachedMetadataContext;
import org.sdase.commons.spring.boot.metadata.context.MetadataContext;

class MetadataContextClientInterceptorTest {

  MetadataContextClientInterceptor metadataRequestInterceptor =
      new MetadataContextClientInterceptor(Set.of("tenant-id", "processes"));

  Enumeration<String> actualHeaders;
  HttpServletRequest givenClientRequestContext;

  @BeforeEach
  void setUp() {
    actualHeaders = Collections.emptyEnumeration();
    givenClientRequestContext = mock(HttpServletRequest.class);
    when(givenClientRequestContext.getHeaders(Mockito.anyString())).thenReturn(actualHeaders);

    MetadataContext.createContext(new DetachedMetadataContext());
  }

  @Test
  void shouldForwardAllConfiguredFields() {
    givenCurrentContext(Map.of("tenant-id", List.of("t-1"), "processes", List.of("p-1")));

    RequestTemplate requestTemplate = new RequestTemplate();

    metadataRequestInterceptor.apply(requestTemplate);

    assertThat(requestTemplate.headers())
        .contains(entry("tenant-id", List.of("t-1")), entry("processes", List.of("p-1")));
  }

  @Test
  void shouldForwardMultipleValues() {
    givenCurrentContext(Map.of("tenant-id", List.of("t-1", "t-2")));

    RequestTemplate requestTemplate = new RequestTemplate();

    metadataRequestInterceptor.apply(requestTemplate);

    assertThat(requestTemplate.headers()).contains(entry("tenant-id", List.of("t-1", "t-2")));
  }

  @Test
  void shouldIgnoreNotConfiguredFields() {
    givenCurrentContext(Map.of("brand", List.of("porsche")));

    RequestTemplate requestTemplate = new RequestTemplate();

    metadataRequestInterceptor.apply(requestTemplate);

    assertThat(requestTemplate.headers()).isEmpty();
  }

  @Test
  void shouldFilterEmptyValues() {
    givenCurrentContext(Map.of("tenant-id", List.of("  ", "t-1", "t-2")));

    RequestTemplate requestTemplate = new RequestTemplate();

    metadataRequestInterceptor.apply(requestTemplate);

    assertThat(requestTemplate.headers()).contains(entry("tenant-id", List.of("t-1", "t-2")));
  }

  @Test
  void shouldNotAddAnythingWithoutConfiguration() {
    var notConfiguredMetadataRequestInterceptor = new MetadataContextClientInterceptor(Set.of());
    RequestTemplate requestTemplate = new RequestTemplate();

    notConfiguredMetadataRequestInterceptor.apply(requestTemplate);

    assertThat(requestTemplate.headers()).isEmpty();
  }

  private void givenCurrentContext(Map<String, List<String>> contextData) {
    var given = new DetachedMetadataContext();
    given.putAll(contextData);
    MetadataContext.createContext(given);
  }
}
