/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.metadata.context;

class MetadataContextHolder {

  private static final ThreadLocal<MetadataContext> METADATA_CONTEXT = new ThreadLocal<>();

  private MetadataContextHolder() {
    // only static methods to handle ThreadLocal
  }

  static MetadataContext get() {
    return getInternal();
  }

  static void set(MetadataContext metadataContext) {
    METADATA_CONTEXT.set(metadataContext);
  }

  static void clear() {
    METADATA_CONTEXT.remove();
  }

  private static MetadataContext getInternal() {
    synchronized (Thread.currentThread()) {
      if (METADATA_CONTEXT.get() == null) {
        METADATA_CONTEXT.set(new UnmodifiableMetadataContext());
      }
    }
    return METADATA_CONTEXT.get();
  }
}
