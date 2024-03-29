/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.test.data.models;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(title = "Electrical engine", description = "An car model with an electrical engine")
@SuppressWarnings("unused")
public class Electrical extends CarModel {

  @NotNull
  @JsonPropertyDescription("The capacity of the battery in kwH")
  @Schema(example = "200")
  private int batteryCapacity;

  public int getBatteryCapacity() {
    return batteryCapacity;
  }

  public Electrical setBatteryCapacity(int batteryCapacity) {
    this.batteryCapacity = batteryCapacity;
    return this;
  }
}
