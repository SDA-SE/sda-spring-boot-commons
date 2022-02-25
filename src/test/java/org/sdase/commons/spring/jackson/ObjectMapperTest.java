/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.jackson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.auth.testing.DisableSdaAuthInitializer;
import org.sdase.commons.spring.jackson.test.Filter;
import org.sdase.commons.spring.jackson.test.JacksonTestApp;
import org.sdase.commons.spring.jackson.test.Person;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(classes = JacksonTestApp.class)
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
class ObjectMapperTest {

  @Autowired private ObjectMapper om;

  @Test
  void deserializeJsonWithUnknownFields() throws Exception {
    // age is not part of model class
    String given = "{\"name\": \"John Doe\", \"age\": 28}";

    Person actual = om.readValue(given, Person.class);

    assertThat(actual)
        .extracting(Person::getName, Person::getDob)
        .containsExactly("John Doe", null);
  }

  @Test
  void readSingleStringAsList() throws Exception {
    String given = "{\"addresses\": \"Main Street 1\\n12345 Gotham City\"}";

    Person actual = om.readValue(given, Person.class);

    assertThat(actual.getAddresses()).containsExactly("Main Street 1\n12345 Gotham City");
  }

  @Test
  void readEnumValue() throws Exception {
    String given = "{\"title\": \"DOCTOR\"}";

    Person actual = om.readValue(given, Person.class);

    assertThat(actual).extracting(Person::getTitle).isEqualTo(Person.Title.DOCTOR);
  }

  @Test
  void readUnknownEnumAsNull() throws Exception {
    String given = "{\"title\": \"DOCTOR_HC\"}";

    Person actual = om.readValue(given, Person.class);

    assertThat(actual).extracting(Person::getTitle).isNull();
  }

  @Test
  void readEnumValueWithDefault() throws Exception {
    String given = "{\"profession\": \"IT\"}";

    Person actual = om.readValue(given, Person.class);

    assertThat(actual).extracting(Person::getProfession).isEqualTo(Person.Profession.IT);
  }

  @Test
  void readUnknownEnumValueAsDefault() throws Exception {
    String given = "{\"profession\": \"CRAFTMANSHIP\"}";

    Person actual = om.readValue(given, Person.class);

    assertThat(actual).extracting(Person::getProfession).isEqualTo(Person.Profession.OTHER);
  }

  @Test
  void writeEmptyBeans() throws Exception {
    String actual = om.writeValueAsString(new Object());

    assertThat(actual).isEqualTo("{}");
  }

  @Test
  void failOnSelfReferenceToAvoidRecursion() {
    Person given = new Person();
    given.setPartner(given);

    assertThatExceptionOfType(JsonMappingException.class)
        .isThrownBy(() -> om.writeValueAsString(given))
        .withMessageContaining("cycle");
  }

  @Test
  void writeNullFields() throws Exception {

    Person given = new Person();

    String actual = om.writeValueAsString(given);

    assertThat(actual)
        .contains("\"name\":null")
        .contains("\"title\":null")
        .contains("\"dob\":null")
        .contains("\"addresses\":null")
        .contains("\"partner\":null")
        .contains("\"profession\":null");
  }

  @Test
  void doNotWriteIgnoredField() throws Exception {

    Person given = new Person().setIdCardNumber("123-456-789");

    String actual = om.writeValueAsString(given);

    assertThat(actual).doesNotContain("idCardNumber", "123-456-789");
  }

  @Test
  void skipIgnoredFieldWhenReading() throws Exception {

    String given = "{\"idCardNumber\": \"123-456-789\"}";

    Person actual = om.readValue(given, Person.class);

    assertThat(actual).isNotNull().extracting(Person::getIdCardNumber).isNull();
  }

  @Test
  void shouldReadSubType() throws Exception {
    String given = "{\"type\":\"my\", \"value\":\"foo\"}";

    Filter actual = om.readValue(given, Filter.class);

    assertThat(actual).isInstanceOf(Filter.MyFilter.class).extracting("value").isEqualTo("foo");
  }

  @Test
  void shouldNotFailForUnknownSubtype() throws Exception {
    String given = "{\"type\":\"notMy\", \"value\":\"foo\"}";

    Filter actual = om.readValue(given, Filter.class);

    assertThat(actual).isNull();
  }
}
