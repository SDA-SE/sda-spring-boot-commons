/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.metadata.context;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

class UnmodifiableMetadataContext implements MetadataContext {

  private final DetachedMetadataContext detachedMetadataContext = new DetachedMetadataContext();

  static UnmodifiableMetadataContext of(DetachedMetadataContext source) {
    var target = new UnmodifiableMetadataContext();
    for (var e : source.entrySet()) {
      target.detachedMetadataContext.put(e.getKey(), new ArrayList<>(e.getValue()));
    }
    return target;
  }

  @Override
  public Set<String> keys() {
    return detachedMetadataContext.keySet();
  }

  @Override
  public List<String> valuesByKey(String key) {
    return detachedMetadataContext.get(key);
  }
}
