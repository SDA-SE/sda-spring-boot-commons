/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka.test;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public class KafkaTestModel {

  @NotBlank private String checkString;

  @NotNull private Integer checkInt;

  private boolean throwNotRetryableException;
  private boolean throwRuntimeException;

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

  public boolean isThrowNotRetryableException() {
    return throwNotRetryableException;
  }

  public KafkaTestModel setThrowNotRetryableException(boolean throwNotRetryableException) {
    this.throwNotRetryableException = throwNotRetryableException;
    return this;
  }

  public boolean isThrowRuntimeException() {
    return throwRuntimeException;
  }

  public KafkaTestModel setThrowRuntimeException(boolean throwRuntimeException) {
    this.throwRuntimeException = throwRuntimeException;
    return this;
  }
}
