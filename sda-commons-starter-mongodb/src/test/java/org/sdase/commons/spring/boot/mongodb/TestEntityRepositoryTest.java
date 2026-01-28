/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mongodb;

// ATTENTION: The source of this class is included in the public documentation.

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.time.ZonedDateTime;
import java.util.Optional;
import org.bson.Document;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.mongodb.test.TestEntity;
import org.sdase.commons.spring.boot.mongodb.test.TestEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.mongodb.autoconfigure.MongoConnectionDetails;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(classes = MongoTestApp.class, webEnvironment = WebEnvironment.MOCK)
class TestEntityRepositoryTest {

  @Autowired MongoClient mongoClient;
  @Autowired MongoConnectionDetails mongoConnectionDetails;

  @Autowired TestEntityRepository testEntityRepository;

  @AfterEach
  @BeforeEach
  void cleanDatabase() {
    getDb().getCollection("testEntity").drop();
  }

  @Test
  void shouldSaveToDatabase() {
    testEntityRepository.save(
        new TestEntity()
            .setId("id_1")
            .setUniqueKey("unique-1")
            .setZonedDateTime(ZonedDateTime.parse("2021-02-21T17:22:53+01:00[Europe/Paris]")));

    assertThat(getDb().getCollection("testEntity").countDocuments()).isOne();
    assertThat(getDb().getCollection("testEntity").find().first())
        .isEqualTo(
            Document.parse(
                """
                {
                  "_class": "org.sdase.commons.spring.boot.mongodb.test.TestEntity",
                  "_id": "id_1",
                  "uniqueKey": "unique-1",
                  "zonedDateTime": {"$date": "2021-02-21T16:22:53Z"}
                }
                """));
  }

  @Test
  void shouldFindInDatabase() {
    getDb()
        .getCollection("testEntity")
        .insertOne(
            Document.parse(
                """
                {
                  "_class": "org.sdase.commons.spring.boot.mongodb.test.TestEntity",
                  "_id": "id_2",
                  "uniqueKey": "unique-2",
                  "zonedDateTime": {"$date": "2021-02-21T17:21:53Z"}
                }
                """));
    Optional<TestEntity> actual = testEntityRepository.findById("id_2");
    assertThat(actual)
        .isPresent()
        .get()
        .extracting(TestEntity::getId, TestEntity::getUniqueKey, TestEntity::getZonedDateTime)
        .containsExactly("id_2", "unique-2", ZonedDateTime.parse("2021-02-21T17:21:53Z"));
  }

  @Test
  void shouldSaveAndFindInDatabase() {
    testEntityRepository.save(
        new TestEntity()
            .setId("id_3")
            .setUniqueKey("unique-3")
            .setZonedDateTime(ZonedDateTime.parse("2021-02-21T17:22:53+01:00[Europe/Paris]")));
    Optional<TestEntity> actual = testEntityRepository.findById("id_3");
    assertThat(actual)
        .isPresent()
        .get()
        .extracting(TestEntity::getId, TestEntity::getUniqueKey, TestEntity::getZonedDateTime)
        .containsExactly("id_3", "unique-3", ZonedDateTime.parse("2021-02-21T16:22:53Z"));
  }

  MongoDatabase getDb() {
    return mongoClient.getDatabase(mongoConnectionDetails.getConnectionString().getDatabase());
  }
}
