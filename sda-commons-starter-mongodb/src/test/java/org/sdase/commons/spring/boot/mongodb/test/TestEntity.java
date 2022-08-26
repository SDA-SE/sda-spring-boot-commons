/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mongodb.test;

import java.time.ZonedDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class TestEntity {

  @Id String id;

  @Indexed(unique = true, name = "unique-key-index")
  String uniqueKey;

  ZonedDateTime zonedDateTime;

  public String getId() {
    return id;
  }

  public TestEntity setId(String id) {
    this.id = id;
    return this;
  }

  public String getUniqueKey() {
    return uniqueKey;
  }

  public TestEntity setUniqueKey(String uniqueKey) {
    this.uniqueKey = uniqueKey;
    return this;
  }

  public ZonedDateTime getZonedDateTime() {
    return zonedDateTime;
  }

  public TestEntity setZonedDateTime(ZonedDateTime zonedDateTime) {
    this.zonedDateTime = zonedDateTime;
    return this;
  }
}
