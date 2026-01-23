/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.cloudevents;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.cloudevents.app.polymorphism.CarLifecycleEvents;
import org.sdase.commons.spring.boot.cloudevents.app.polymorphism.CarLifecycleEvents.CarManufactured;
import org.sdase.commons.spring.boot.cloudevents.app.polymorphism.CarLifecycleEvents.CarScrapped;
import org.sdase.commons.spring.boot.web.jackson.SdaObjectMapperConfiguration;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

class PolymorphismTest {

  static JsonMapper jsonMapper =
      new SdaObjectMapperConfiguration().sdaObjectMapperBuilder().build();

  @Test
  void shouldDeserializeToManufactured() {
    var given =
        """
        {
          "specversion": "1.0",
          "id": "car-123",
          "source": "/SDA-SE/test",
          "type": "se.sda.car.manufactured",
          "subject": "car-123",
          "datacontenttype": "application/json",
          "time": "2023-08-18T16:02:22.203208+02:00",
          "data": {
            "brand": "Aston Martin",
            "model": "DB5"
          }
        }
        """;
    var actual = jsonMapper.readValue(given, CarLifecycleEvents.class);
    assertThat(actual)
        .isInstanceOf(CarManufactured.class)
        .extracting(CloudEventV1::getType)
        .isEqualTo("se.sda.car.manufactured");
    assertThat(((CarManufactured) actual).getData())
        .extracting(
            CarManufactured.CarManufacturedData::brand, CarManufactured.CarManufacturedData::model)
        .contains("Aston Martin", "DB5");
  }

  @Test
  void shouldSerializeManufactured() {
    var givenId = UUID.randomUUID().toString();
    var given =
        new CarManufactured()
            .setData(new CarManufactured.CarManufacturedData("Aston Martin", "DB5"))
            .setId(givenId)
            .setSource(URI.create("/SDA-SE/test"))
            .setSubject("car-123");
    String actual = jsonMapper.writeValueAsString(given);
    String expected =
        """
        {
          "specversion": "1.0",
          "id": "%s",
          "source": "/SDA-SE/test",
          "type": "se.sda.car.manufactured",
          "subject": "car-123",
          "datacontenttype": "application/json",
          "time": %s,
          "data": {
            "brand": "Aston Martin",
            "model": "DB5"
          }
        }
        """
            .formatted(givenId, jsonMapper.writeValueAsString(given.getTime()));
    assertThat(jsonMapper.readValue(actual, JsonNode.class))
        .describedAs("%s%nis not equal to%n%s", actual, expected)
        .isEqualTo(jsonMapper.readValue(expected, JsonNode.class));
  }

  @Test
  void shouldDeserializeToScrapped() {
    var given =
        """
        {
          "specversion": "1.0",
          "id": "car-123",
          "source": "/SDA-SE/test",
          "type": "se.sda.car.scrapped",
          "subject": "car-123",
          "datacontenttype": "application/json",
          "time": "2023-08-18T16:02:22.203208+02:00",
          "data": { "reason": "ACCIDENT" }
        }
        """;
    var actual = jsonMapper.readValue(given, CarLifecycleEvents.class);
    assertThat(actual)
        .isInstanceOf(CarScrapped.class)
        .extracting(CloudEventV1::getType)
        .isEqualTo("se.sda.car.scrapped");
    assertThat(((CarScrapped) actual).getData())
        .extracting(CarScrapped.CarScrappedData::reason)
        .isEqualTo(CarScrapped.CarScrappedData.ScrapReason.ACCIDENT);
  }

  @Test
  void shouldSerializeScrapped() {
    var givenId = UUID.randomUUID().toString();
    var given =
        new CarScrapped()
            .setData(
                new CarScrapped.CarScrappedData(
                    CarScrapped.CarScrappedData.ScrapReason.TECHNICAL_DAMAGE))
            .setId(givenId)
            .setSource(URI.create("/SDA-SE/test"))
            .setSubject("car-123");
    String actual = jsonMapper.writeValueAsString(given);
    String expected =
        """
        {
          "specversion": "1.0",
          "id": "%s",
          "source": "/SDA-SE/test",
          "type": "se.sda.car.scrapped",
          "subject": "car-123",
          "datacontenttype": "application/json",
          "time": %s,
          "data": { "reason": "TECHNICAL_DAMAGE" }
        }
        """
            .formatted(givenId, jsonMapper.writeValueAsString(given.getTime()));
    assertThat(jsonMapper.readValue(actual, ObjectNode.class))
        .describedAs("%s%nis not equal to%n%s", actual, expected)
        .isEqualTo(jsonMapper.readValue(expected, ObjectNode.class));
  }
}
