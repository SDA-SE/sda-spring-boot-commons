/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.testing.mongodb.ExternalMongoAutoConfiguration;
import org.springframework.mock.env.MockEnvironment;

class ExternalMongoAutoConfigurationWithFlapdoodleTest {

  ExternalMongoAutoConfiguration externalMongoAutoConfiguration =
      new ExternalMongoAutoConfiguration();

  @Test
  void shouldExcludeFlapDoodleIfPropertyIsSet() {
    var mockEnvironment =
        new MockEnvironment().withProperty("test.mongodb.connection.string", "mongodb://localhost");
    externalMongoAutoConfiguration.postProcessEnvironment(mockEnvironment, null);
    assertThat(mockEnvironment.getProperty("spring.autoconfigure.exclude"))
        .isEqualTo("de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration");
  }

  @Test
  void shouldKeepExistingExcludesIfPropertyIsSet() {
    var mockEnvironment =
        new MockEnvironment()
            .withProperty("test.mongodb.connection.string", "mongodb://localhost")
            .withProperty("spring.autoconfigure.exclude", "com.example.ToBeExcluded");
    externalMongoAutoConfiguration.postProcessEnvironment(mockEnvironment, null);
    assertThat(mockEnvironment.getProperty("spring.autoconfigure.exclude"))
        .isEqualTo(
            "com.example.ToBeExcluded,de.flapdoodle.embed.mongo.spring.autoconfigure.EmbeddedMongoAutoConfiguration");
  }

  @Test
  void shouldSetSpringDataMongoUri() {
    var mockEnvironment =
        new MockEnvironment().withProperty("test.mongodb.connection.string", "mongodb://localhost");
    externalMongoAutoConfiguration.postProcessEnvironment(mockEnvironment, null);
    assertThat(mockEnvironment.getProperty("spring.data.mongodb.uri"))
        .isEqualTo("mongodb://localhost");
  }

  @Test
  void shouldNotChangeEnvironmentWithoutProperty() {
    var mockEnvironment =
        new MockEnvironment()
            .withProperty("spring.autoconfigure.exclude", "com.example.ToBeExcluded");
    externalMongoAutoConfiguration.postProcessEnvironment(mockEnvironment, null);
    assertThat(mockEnvironment.getProperty("spring.data.mongodb.uri")).isNull();
    assertThat(mockEnvironment.getProperty("spring.autoconfigure.exclude"))
        .isEqualTo("com.example.ToBeExcluded");
  }
}
