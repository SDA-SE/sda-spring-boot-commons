/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "A list of cars")
public class Cars {
  @Schema(description = "The cars")
  private List<Car> cars;

  public List<Car> getCars() {
    return cars;
  }

  public Cars setCars(List<Car> cars) {
    this.cars = cars;
    return this;
  }
}
