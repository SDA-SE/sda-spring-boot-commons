/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi.models;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaDescription;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaExamples;
import com.kjetland.jackson.jsonSchema.annotations.JsonSchemaTitle;
import javax.validation.constraints.NotNull;

@JsonSchemaTitle("Combustion engine")
@JsonSchemaDescription("An car model with a combustion engine")
public class Combustion extends CarModel {

  @JsonPropertyDescription("The capacity of the tank in liter")
  @JsonSchemaExamples(value = {"95"})
  @NotNull
  private int tankVolume;

  public int getTankVolume() {
    return tankVolume;
  }

  public Combustion setTankVolume(int tankVolume) {
    this.tankVolume = tankVolume;
    return this;
  }
}
