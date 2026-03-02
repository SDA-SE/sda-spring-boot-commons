/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mongodb.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bson.BsonBinarySubType.UUID_LEGACY;
import static org.bson.BsonBinarySubType.UUID_STANDARD;

import java.util.List;
import java.util.UUID;
import org.bson.BsonBinary;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.internal.UuidHelper;
import org.bson.types.Binary;
import org.junit.jupiter.api.Test;

class MongoDBUuidRepresentationMigratorTest {

  @Test
  void shouldMigrateTopLevelLegacyUuid() {
    UUID uuid = UUID.randomUUID();

    // Create legacy binary
    byte[] legacyBytes = UuidHelper.encodeUuidToBinary(uuid, UuidRepresentation.JAVA_LEGACY);
    Binary legacyBinary = new Binary(UUID_LEGACY.getValue(), legacyBytes);

    Document root = new Document("id", legacyBinary);

    boolean changed =
        MongoDBUuidRepresentationMigrator.convertUuidValuesFromLegacyToStandardFor(root);

    assertThat(changed).isTrue();

    Object migratedValue = root.get("id");
    assertThat(migratedValue).isInstanceOf(BsonBinary.class);

    BsonBinary standardBinary = (BsonBinary) migratedValue;
    assertThat(standardBinary.getType()).isEqualTo(UUID_STANDARD.getValue());

    UUID decoded = standardBinary.asUuid(UuidRepresentation.STANDARD);
    assertThat(decoded).isEqualTo(uuid);
  }

  @Test
  void shouldMigrateNestedDocumentLegacyUuid() {
    UUID uuid = UUID.randomUUID();

    byte[] legacyBytes = UuidHelper.encodeUuidToBinary(uuid, UuidRepresentation.JAVA_LEGACY);
    Binary legacyBinary = new Binary(UUID_LEGACY.getValue(), legacyBytes);

    Document nested = new Document("nestedId", legacyBinary);
    Document root = new Document("child", nested);

    boolean changed =
        MongoDBUuidRepresentationMigrator.convertUuidValuesFromLegacyToStandardFor(root);

    assertThat(changed).isTrue();

    BsonBinary migrated = (BsonBinary) nested.get("nestedId");
    assertThat(migrated.getType()).isEqualTo(UUID_STANDARD.getValue());

    UUID decoded = migrated.asUuid(UuidRepresentation.STANDARD);
    assertThat(decoded).isEqualTo(uuid);
  }

  @Test
  void shouldReturnFalseWhenNoLegacyUuidPresent() {
    UUID uuid = UUID.randomUUID();

    byte[] standardBytes = UuidHelper.encodeUuidToBinary(uuid, UuidRepresentation.STANDARD);
    BsonBinary standardBinary = new BsonBinary(UUID_STANDARD, standardBytes);

    Document root = new Document("id", standardBinary);

    boolean changed =
        MongoDBUuidRepresentationMigrator.convertUuidValuesFromLegacyToStandardFor(root);

    assertThat(changed).isFalse();
    assertThat(root).containsEntry("id", standardBinary);
  }

  @Test
  void shouldIgnoreNonBinaryFields() {
    Document root =
        new Document("name", "test")
            .append("count", 42)
            .append("child", new Document("value", "nested"));

    boolean changed =
        MongoDBUuidRepresentationMigrator.convertUuidValuesFromLegacyToStandardFor(root);

    assertThat(changed).isFalse();
  }

  @Test
  void shouldMigrateMultipleLegacyUuids() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();

    byte[] legacyBytes1 = UuidHelper.encodeUuidToBinary(uuid1, UuidRepresentation.JAVA_LEGACY);
    byte[] legacyBytes2 = UuidHelper.encodeUuidToBinary(uuid2, UuidRepresentation.JAVA_LEGACY);

    Document root =
        new Document("id1", new Binary(UUID_LEGACY.getValue(), legacyBytes1))
            .append("child", new Document("id2", new Binary(UUID_LEGACY.getValue(), legacyBytes2)));

    boolean changed =
        MongoDBUuidRepresentationMigrator.convertUuidValuesFromLegacyToStandardFor(root);

    assertThat(changed).isTrue();

    BsonBinary migrated1 = (BsonBinary) root.get("id1");
    BsonBinary migrated2 = (BsonBinary) ((Document) root.get("child")).get("id2");

    assertThat(migrated1.getType()).isEqualTo(UUID_STANDARD.getValue());
    assertThat(migrated2.getType()).isEqualTo(UUID_STANDARD.getValue());

    assertThat(migrated1.asUuid(UuidRepresentation.STANDARD)).isEqualTo(uuid1);
    assertThat(migrated2.asUuid(UuidRepresentation.STANDARD)).isEqualTo(uuid2);
  }

  @Test
  void shouldMigrateNestedLists() {
    UUID uuid1 = UUID.randomUUID();
    UUID uuid2 = UUID.randomUUID();
    UUID uuid3 = UUID.randomUUID();
    UUID uuid4 = UUID.randomUUID();
    UUID uuid5 = UUID.randomUUID();
    byte[] legacyBytes1 = UuidHelper.encodeUuidToBinary(uuid1, UuidRepresentation.JAVA_LEGACY);
    byte[] legacyBytes2 = UuidHelper.encodeUuidToBinary(uuid2, UuidRepresentation.JAVA_LEGACY);
    byte[] legacyBytes3 = UuidHelper.encodeUuidToBinary(uuid3, UuidRepresentation.JAVA_LEGACY);
    byte[] legacyBytes4 = UuidHelper.encodeUuidToBinary(uuid4, UuidRepresentation.JAVA_LEGACY);
    byte[] legacyBytes5 = UuidHelper.encodeUuidToBinary(uuid5, UuidRepresentation.JAVA_LEGACY);

    Document dog1 =
        new Document("_id", new Binary(UUID_LEGACY.getValue(), legacyBytes1))
            .append("name", "Rufus");
    Document dog2 =
        new Document("_id", new Binary(UUID_LEGACY.getValue(), legacyBytes2))
            .append("name", "Buddy");
    Document cat1 =
        new Document("_id", new Binary(UUID_LEGACY.getValue(), legacyBytes3))
            .append("name", "Whiskers");
    Document cat2 =
        new Document("_id", new Binary(UUID_LEGACY.getValue(), legacyBytes4))
            .append("name", "Fluffy");
    Document dogs = new Document("dogs", List.of(dog1, dog2));
    Document cats = new Document("cats", List.of(cat1, cat2));
    Document root =
        new Document("_id", new Binary(UUID_LEGACY.getValue(), legacyBytes5))
            .append("pets", List.of(dogs, cats));

    boolean changed =
        MongoDBUuidRepresentationMigrator.convertUuidValuesFromLegacyToStandardFor(root);

    assertThat(changed).isTrue();

    // Root _id check
    BsonBinary rootId = (BsonBinary) root.get("_id");
    assertThat(rootId.getType()).isEqualTo(UUID_STANDARD.getValue());
    assertThat(rootId.asUuid(UuidRepresentation.STANDARD)).isEqualTo(uuid5);

    // Pets list: dogs and cats
    List<Document> pets = (List<Document>) root.get("pets");

    // First element: dogs
    Document dogsDoc = pets.getFirst();
    List<Document> dogList = (List<Document>) dogsDoc.get("dogs");

    BsonBinary dog1Id = (BsonBinary) dogList.getFirst().get("_id");
    assertThat(dog1Id.getType()).isEqualTo(UUID_STANDARD.getValue());
    assertThat(dog1Id.asUuid(UuidRepresentation.STANDARD)).isEqualTo(uuid1);

    BsonBinary dog2Id = (BsonBinary) dogList.get(1).get("_id");
    assertThat(dog2Id.getType()).isEqualTo(UUID_STANDARD.getValue());
    assertThat(dog2Id.asUuid(UuidRepresentation.STANDARD)).isEqualTo(uuid2);

    // Second element: cats
    Document catsDoc = pets.get(1);
    List<Document> catList = (List<Document>) catsDoc.get("cats");

    BsonBinary cat1Id = (BsonBinary) catList.getFirst().get("_id");
    assertThat(cat1Id.getType()).isEqualTo(UUID_STANDARD.getValue());
    assertThat(cat1Id.asUuid(UuidRepresentation.STANDARD)).isEqualTo(uuid3);

    BsonBinary cat2Id = (BsonBinary) catList.get(1).get("_id");
    assertThat(cat2Id.getType()).isEqualTo(UUID_STANDARD.getValue());
    assertThat(cat2Id.asUuid(UuidRepresentation.STANDARD)).isEqualTo(uuid4);
  }
}
