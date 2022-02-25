/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.jackson.test;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true)
@JsonSubTypes({@JsonSubTypes.Type(value = Filter.MyFilter.class, name = "my")})
public interface Filter {

  class MyFilter implements Filter {
    private String value;

    public MyFilter setValue(String value) {
      this.value = value;
      return this;
    }

    public String getValue() {
      return value;
    }
  }
}
