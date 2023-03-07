/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka.test;

import org.sdase.commons.spring.boot.web.metadata.DetachedMetadataContext;
import org.springframework.stereotype.Component;

@Component
public class MetadataCollector {

  private DetachedMetadataContext currentContext = null;

  public void setCurrentContext(DetachedMetadataContext context) {
    currentContext = context;
  }

  public DetachedMetadataContext getCurrentContext() {
    return currentContext;
  }

  public void clearCurrentContext() {
    currentContext = null;
  }
}
