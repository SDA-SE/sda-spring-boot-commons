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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

@Schema(title = "Car manufactured", description = "A new car was manufactured")
@SuppressWarnings("unused")
public class CarManufactured extends BaseEvent {

  @NotBlank
  @Schema(description = "The registration of the vehicle", example = "BB324A81")
  private String vehicleRegistration;

  @NotNull
  @JsonPropertyDescription("The time of manufacturing")
  private Instant date;

  @NotNull
  @JsonPropertyDescription("The model of the car")
  private CarModel model;

  public String getVehicleRegistration() {
    return vehicleRegistration;
  }

  public CarManufactured setVehicleRegistration(String vehicleRegistration) {
    this.vehicleRegistration = vehicleRegistration;
    return this;
  }

  public Instant getDate() {
    return date;
  }

  public CarManufactured setDate(Instant date) {
    this.date = date;
    return this;
  }

  public CarModel getModel() {
    return model;
  }

  public CarManufactured setModel(CarModel model) {
    this.model = model;
    return this;
  }
}
