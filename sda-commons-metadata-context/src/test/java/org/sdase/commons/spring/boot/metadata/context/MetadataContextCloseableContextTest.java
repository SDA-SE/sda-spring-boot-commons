/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.metadata.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetadataContextCloseableContextTest {

  @BeforeEach
  @AfterEach
  void clear() {
    MetadataContextHolder.clear();
  }

  @Test
  void shouldPrepareAndResetContext() {
    var oldContext = new DetachedMetadataContext();
    oldContext.put("tenant-id", List.of("t-old"));

    MetadataContext.createContext(oldContext);

    var context = new DetachedMetadataContext();
    context.put("tenant-id", List.of("t1"));

    try (var ignored = MetadataContext.createCloseableContext(context)) {
      var actualInside = MetadataContext.current();
      assertThat(actualInside.keys()).containsExactly("tenant-id");
      assertThat(actualInside.valuesByKey("tenant-id")).containsExactly("t1");
    }

    var actualAfter = MetadataContext.current();
    assertThat(actualAfter.keys()).containsExactly("tenant-id");
    assertThat(actualAfter.valuesByKey("tenant-id")).containsExactly("t-old");
  }
}
