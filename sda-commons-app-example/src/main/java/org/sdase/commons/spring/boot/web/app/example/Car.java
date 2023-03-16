/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A car")
public class Car {
  @Schema(description = "The license plate")
  private String licensePlate;

  public String getLicensePlate() {
    return licensePlate;
  }

  public Car setLicensePlate(String licensePlate) {
    this.licensePlate = licensePlate;
    return this;
  }
}
