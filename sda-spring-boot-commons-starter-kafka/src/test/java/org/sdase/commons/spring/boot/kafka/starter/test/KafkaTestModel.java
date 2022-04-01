/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.kafka.starter.test;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class KafkaTestModel {

  @NotBlank private String checkString;

  @NotNull private Integer checkInt;

  public String getCheckString() {
    return checkString;
  }

  public KafkaTestModel setCheckString(String checkString) {
    this.checkString = checkString;
    return this;
  }

  public Integer getCheckInt() {
    return checkInt;
  }

  public KafkaTestModel setCheckInt(Integer checkInt) {
    this.checkInt = checkInt;
    return this;
  }
}
