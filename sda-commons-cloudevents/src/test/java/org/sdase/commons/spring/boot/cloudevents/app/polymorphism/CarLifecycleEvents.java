/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.cloudevents.app.polymorphism;

import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.EXISTING_PROPERTY;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.Id.NAME;
import static org.sdase.commons.spring.boot.cloudevents.app.polymorphism.CarLifecycleEvents.MANUFACTURED;
import static org.sdase.commons.spring.boot.cloudevents.app.polymorphism.CarLifecycleEvents.SCRAPPED;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.sdase.commons.spring.boot.cloudevents.CloudEventV1;

@JsonTypeInfo(use = NAME, property = "type", visible = true, include = EXISTING_PROPERTY)
@JsonSubTypes({
  @Type(value = CarLifecycleEvents.CarManufactured.class, name = MANUFACTURED),
  @Type(value = CarLifecycleEvents.CarScrapped.class, name = SCRAPPED)
})
public abstract class CarLifecycleEvents<T> extends CloudEventV1<T> {

  static final String MANUFACTURED = "se.sda.car.manufactured";
  static final String SCRAPPED = "se.sda.car.scrapped";

  public static class CarManufactured
      extends CarLifecycleEvents<CarManufactured.CarManufacturedData> {
    public CarManufactured() {
      super();
      super.setType(MANUFACTURED);
    }

    public record CarManufacturedData(String brand, String model) {}
  }

  public static class CarScrapped extends CarLifecycleEvents<CarScrapped.CarScrappedData> {
    public CarScrapped() {
      super();
      super.setType(SCRAPPED);
    }

    public record CarScrappedData(ScrapReason reason) {
      public enum ScrapReason {
        ACCIDENT,
        TECHNICAL_DAMAGE
      }
    }
  }
}
