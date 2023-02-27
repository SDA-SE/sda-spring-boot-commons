/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mongodb.metadata.test.model;

import org.sdase.commons.spring.boot.web.metadata.DetachedMetadataContext;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class BusinessEntity {

  @Id private String id;

  private DetachedMetadataContext metadata;

  public String getId() {
    return id;
  }

  public BusinessEntity setId(String id) {
    this.id = id;
    return this;
  }

  public DetachedMetadataContext getMetadata() {
    return metadata;
  }

  public BusinessEntity setMetadata(DetachedMetadataContext metadata) {
    this.metadata = metadata;
    return this;
  }
}
