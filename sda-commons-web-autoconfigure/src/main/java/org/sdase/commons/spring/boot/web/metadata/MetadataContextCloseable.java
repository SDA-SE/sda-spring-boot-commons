/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata;

import java.io.Closeable;

/**
 * A {@link Closeable} that restores a {@link MetadataContext} without throwing an {@link Exception}
 * when it {@linkplain #close() closes}.
 */
public class MetadataContextCloseable implements Closeable {

  private final DetachedMetadataContext detachedMetadataContext;

  MetadataContextCloseable(DetachedMetadataContext detachedMetadataContextToRestore) {
    this.detachedMetadataContext = detachedMetadataContextToRestore;
  }

  @Override
  public void close() {
    MetadataContext.createContext(detachedMetadataContext);
  }
}
