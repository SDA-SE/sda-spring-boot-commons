/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class DetachedMetadataContextTest {

  DetachedMetadataContext detachedMetadataContext = new DetachedMetadataContext();

  @Test
  void shouldIdentifyEmptyMetadata() {
    assertThat(detachedMetadataContext.isEffectivelyEmpty()).isTrue();
  }

  @Test
  void shouldIdentifyEmptyMetadataWithEmptyLists() {
    detachedMetadataContext.put("tenant-id", List.of());
    detachedMetadataContext.put("processes", List.of());
    assertThat(detachedMetadataContext.isEffectivelyEmpty()).isTrue();
  }

  @Test
  void shouldIdentifyEmptyMetadataWithNullValues() {
    detachedMetadataContext.put("tenant-id", null);
    detachedMetadataContext.put("processes", null);
    assertThat(detachedMetadataContext.isEffectivelyEmpty()).isTrue();
  }

  @Test
  void shouldIdentifyNonEmptyMetadataWithValues() {
    detachedMetadataContext.put("tenant-id", List.of("t-1"));
    assertThat(detachedMetadataContext.isEffectivelyEmpty()).isFalse();
  }

  @Test
  void shouldIdentifyNonEmptyMetadataWithValuesAndNull() {
    detachedMetadataContext.put("tenant-id", List.of("t-1"));
    detachedMetadataContext.put("processes", null);
    assertThat(detachedMetadataContext.isEffectivelyEmpty()).isFalse();
  }

  @Test
  void shouldIdentifyNonEmptyMetadataWithValuesAndEmptyValues() {
    detachedMetadataContext.put("tenant-id", List.of("t-1"));
    detachedMetadataContext.put("processes", List.of());
    assertThat(detachedMetadataContext.isEffectivelyEmpty()).isFalse();
  }
}
