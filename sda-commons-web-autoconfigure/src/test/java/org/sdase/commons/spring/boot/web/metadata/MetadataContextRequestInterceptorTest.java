/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.MultivaluedMap;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetadataContextRequestInterceptorTest {

  @BeforeEach
  void clear() {
    MetadataContextHolder.clear();
  }

  @Test
  void shouldStoreOnlyMetadataContextFieldsInRequest() {
    MetadataContextRequestInterceptor m =
        new MetadataContextRequestInterceptor(Set.of("tenant-id", "processes"));
    var headers = new MultivaluedStringMap();
    headers.put("tenant-id", List.of("t1"));
    headers.put("processes", List.of("p1", "p2"));
    headers.put("other-header", List.of("hello"));
    m.preHandle(requestWithHeaders(headers), null, null);

    assertThat(MetadataContext.current().keys())
        .containsExactlyInAnyOrder("tenant-id", "processes");
    assertThat(MetadataContext.current().valuesByKey("tenant-id")).containsExactly("t1");
    assertThat(MetadataContext.current().valuesByKey("processes")).containsExactly("p1", "p2");
  }

  @Test
  void shouldSplitFromSingleValue() {
    MetadataContextRequestInterceptor m =
        new MetadataContextRequestInterceptor(Set.of("tenant-id"));
    var headers = new MultivaluedStringMap();
    headers.put("tenant-id", List.of("t1,t2, t3 , t4"));
    m.preHandle(requestWithHeaders(headers), null, null);

    assertThat(MetadataContext.current().keys()).containsExactlyInAnyOrder("tenant-id");
    assertThat(MetadataContext.current().valuesByKey("tenant-id"))
        .containsExactly("t1", "t2", "t3", "t4");
  }

  @Test
  void shouldSplitFromMultipleValues() {
    MetadataContextRequestInterceptor m =
        new MetadataContextRequestInterceptor(Set.of("tenant-id"));
    var headers = new MultivaluedStringMap();
    headers.put("tenant-id", List.of("t1,t2", " t3 , t4"));
    m.preHandle(requestWithHeaders(headers), null, null);

    assertThat(MetadataContext.current().keys()).containsExactlyInAnyOrder("tenant-id");
    assertThat(MetadataContext.current().valuesByKey("tenant-id"))
        .containsExactly("t1", "t2", "t3", "t4");
  }

  @Test
  void shouldStoreIncompleteMetadataContextFieldsInRequest() {
    MetadataContextRequestInterceptor m =
        new MetadataContextRequestInterceptor(Set.of("tenant-id", "processes"));
    var headers = new MultivaluedStringMap();
    headers.put("tenant-id", List.of("t1"));
    headers.put("other-header", List.of("hello"));
    m.preHandle(requestWithHeaders(headers), null, null);

    assertThat(MetadataContext.current().keys())
        .containsExactlyInAnyOrder("tenant-id", "processes");
    assertThat(MetadataContext.current().valuesByKey("tenant-id")).containsExactly("t1");
    assertThat(MetadataContext.current().valuesByKey("processes")).isEmpty();
  }

  @Test
  void shouldClearAfterRequest() throws Exception {
    MetadataContextRequestInterceptor m =
        new MetadataContextRequestInterceptor(Set.of("tenant-id", "processes"));
    var before = new DetachedMetadataContext();
    before.put("tenant-id", List.of("t1"));
    MetadataContext.createContext(before);

    // precondition
    assertThat(MetadataContext.current().keys()).containsExactly("tenant-id");

    m.afterCompletion(mock(HttpServletRequest.class), null, null, null);

    assertThat(MetadataContext.current().keys()).isEmpty();
  }

  HttpServletRequest requestWithHeaders(MultivaluedMap<String, String> headers) {
    HttpServletRequest mock = mock(HttpServletRequest.class);
    headers.forEach(
        (key, value) -> when(mock.getHeaders(key)).thenReturn(Collections.enumeration(value)));
    return mock;
  }
}
