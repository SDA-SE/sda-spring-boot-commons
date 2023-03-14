/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka.test;

import org.sdase.commons.spring.boot.metadata.context.DetachedMetadataContext;
import org.springframework.stereotype.Component;

@Component
public class MetadataCollector {

  private DetachedMetadataContext lastCollectedContext = null;

  public DetachedMetadataContext getLastCollectedContext() {
    return lastCollectedContext;
  }

  public MetadataCollector setLastCollectedContext(DetachedMetadataContext lastCollectedContext) {
    this.lastCollectedContext = lastCollectedContext;
    return this;
  }

  public void clearLastCollectedContext() {
    lastCollectedContext = null;
  }
}
