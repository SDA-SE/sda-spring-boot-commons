/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mongodb.metadata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import com.mongodb.client.MongoCollection;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.mongodb.MongoTestApp;
import org.sdase.commons.spring.boot.mongodb.metadata.test.model.BusinessEntity;
import org.sdase.commons.spring.boot.web.metadata.DetachedMetadataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;

@SpringBootTest(
    classes = MongoTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MetadataContextStorageIntegrationTest {

  static final Logger LOG = LoggerFactory.getLogger(MetadataContextStorageIntegrationTest.class);

  @Autowired MongoOperations mongoOperations;

  @BeforeEach
  void beforeEach() {
    mongoOperations.dropCollection("businessEntity");
  }

  @Test
  void shouldSaveMetadataContext() {
    var givenContext = new DetachedMetadataContext();
    givenContext.put("tenant-id", List.of("tenant-1"));
    givenContext.put("processes", List.of("p-1", "p-2"));
    var givenEntity =
        new BusinessEntity().setId(UUID.randomUUID().toString()).setMetadata(givenContext);

    mongoOperations.insert(givenEntity);

    var collection = businessEntityCollection();
    var doc = collection.find().first();

    LOG.info("Stored: {}", doc);

    assertThat(doc)
        .isNotNull()
        .extracting("metadata")
        .isNotNull()
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsExactly(
            entry("tenant-id", List.of("tenant-1")), entry("processes", List.of("p-1", "p-2")));
  }

  @Test
  void shouldLoadMetadataContextWithoutClassInfo() {

    var given =
        Document.parse(
            "{"
                + "\"_id\": \"038EBE14-37FB-4531-BA83-CE52B38D5EB0\","
                + "\"metadata\": {"
                + "\"tenant-id\": [\"tenant1\"],"
                + "\"processes\": [\"p1\",\"p2\"]"
                + "}"
                + "}");

    businessEntityCollection().insertOne(given);

    var actual = mongoOperations.findAll(BusinessEntity.class).stream().findFirst().orElse(null);

    LOG.info("Loaded: {}", actual);

    assertThat(actual)
        .isNotNull()
        .extracting(BusinessEntity::getMetadata)
        .isNotNull()
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsExactly(
            entry("tenant-id", List.of("tenant1")), entry("processes", List.of("p1", "p2")));
  }

  @Test
  void shouldLoadMetadataContextWithClassInfo() {

    var given =
        Document.parse(
            "{"
                + "\"_id\": \"038EBE14-37FB-4531-BA83-CE52B38D5EB0\","
                + "\"_class\": \"org.sdase.commons.server.spring.data.mongo.metadata.test.model.BusinessEntity\","
                + "\"metadata\": {"
                + "\"tenant-id\": [\"tenant1\"],"
                + "\"processes\": [\"p1\",\"p2\"]"
                + "}"
                + "}");

    businessEntityCollection().insertOne(given);

    var actual = mongoOperations.findAll(BusinessEntity.class).stream().findFirst().orElse(null);

    LOG.info("Loaded: {}", actual);

    assertThat(actual)
        .isNotNull()
        .extracting(BusinessEntity::getMetadata)
        .isNotNull()
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsExactly(
            entry("tenant-id", List.of("tenant1")), entry("processes", List.of("p1", "p2")));
  }

  @Test
  void shouldLoadMetadataContextWithCompatibilityClassInfo() {

    var given =
        Document.parse(
            "{"
                + "\"_id\": \"038EBE14-37FB-4531-BA83-CE52B38D5EB0\","
                + "\"className\": \"org.sdase.commons.server.spring.data.mongo.metadata.test.model.BusinessEntity\","
                + "\"metadata\": {"
                + "\"tenant-id\": [\"tenant1\"],"
                + "\"processes\": [\"p1\",\"p2\"]"
                + "}"
                + "}");

    businessEntityCollection().insertOne(given);

    var actual = mongoOperations.findAll(BusinessEntity.class).stream().findFirst().orElse(null);

    LOG.info("Loaded: {}", actual);

    assertThat(actual)
        .isNotNull()
        .extracting(BusinessEntity::getMetadata)
        .isNotNull()
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsExactly(
            entry("tenant-id", List.of("tenant1")), entry("processes", List.of("p1", "p2")));
  }

  @Test
  void shouldLoadSavedMetadataContext() {
    var givenContext = new DetachedMetadataContext();
    givenContext.put("tenant-id", List.of("tenant-1"));
    givenContext.put("processes", List.of("p-1", "p-2"));
    var givenEntity =
        new BusinessEntity().setId(UUID.randomUUID().toString()).setMetadata(givenContext);

    mongoOperations.insert(givenEntity);

    var actual = mongoOperations.findAll(BusinessEntity.class).stream().findFirst().orElse(null);

    LOG.info("Loaded: {}", actual);

    assertThat(actual)
        .isNotNull()
        .extracting(BusinessEntity::getMetadata)
        .isNotNull()
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsExactly(
            entry("tenant-id", List.of("tenant-1")), entry("processes", List.of("p-1", "p-2")));
  }

  private MongoCollection<Document> businessEntityCollection() {
    return mongoOperations.getCollection("businessEntity");
  }
}
