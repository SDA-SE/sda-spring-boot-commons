/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.jackson;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.jackson.test.Filter;
import org.sdase.commons.spring.boot.web.jackson.test.JacksonTestApp;
import org.sdase.commons.spring.boot.web.jackson.test.Person;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;

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

    assertThatExceptionOfType(JacksonException.class)
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

  @Test
  void shouldNotWriteValidJsonOnSerializationError() {
    var iterator = new FailingCloseableIterator();

    var output = new ByteArrayOutputStream();

    try (JsonGenerator generator = om.createGenerator(output)) {
      generator.writePOJO(Map.of("items", iterator));
    } catch (Exception ignored) {
      // nothing to do
    }
    var expected = new StringBuilder();
    for (int i = 1; i < 1_001; i++) {
      expected.append("\"").append(i).append("\",");
    }
    expected.deleteCharAt(expected.length() - 1);
    String actual = output.toString(UTF_8);
    // verify expected content
    assertThat(actual).isEqualToIgnoringWhitespace("{\"items\":[" + expected);
    // verify not readable
    assertThatExceptionOfType(JacksonException.class)
        .isThrownBy(() -> om.readValue(actual, Object.class));
  }

  static class FailingCloseableIterator implements Iterator<String>, Closeable {

    final AtomicInteger i = new AtomicInteger();

    @Override
    public void close() {
      // do nothing on close
    }

    @Override
    public boolean hasNext() {
      return i.get() < 2_000;
    }

    @Override
    public String next() {
      var current = i.incrementAndGet();
      if (current > 1_000) {
        throw new IllegalStateException();
      }
      return "" + current;
    }
  }
}
