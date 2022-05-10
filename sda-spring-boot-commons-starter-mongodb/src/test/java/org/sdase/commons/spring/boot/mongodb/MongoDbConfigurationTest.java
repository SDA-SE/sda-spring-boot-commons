/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.mongodb;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.ZonedDateTime;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.mongodb.test.TestEntity;
import org.sdase.commons.spring.boot.mongodb.test.TestEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.dao.DuplicateKeyException;

@SpringBootTest(classes = MongoTestApp.class, webEnvironment = WebEnvironment.NONE)
class MongoDbConfigurationTest {

  @Autowired TestEntityRepository testEntityRepository;

  @Test
  void shouldThrowDuplicateKeyExceptionWhenUniqueIndexIsStoredTwice() {
    testEntityRepository.save(
        new TestEntity()
            .setId("id_1")
            .setUniqueKey("UNIQUE")
            .setZonedDateTime(ZonedDateTime.parse("2021-02-21T17:22:53+01:00[Europe/Paris]")));
    final var entity = new TestEntity().setId("id_2").setUniqueKey("UNIQUE");
    assertThatThrownBy(() -> testEntityRepository.save(entity))
        .isInstanceOf(DuplicateKeyException.class);
  }
}
