/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mongodb.migration;

import static org.assertj.core.api.Assertions.assertThat;

import com.mongodb.client.model.Filters;
import java.util.UUID;
import org.bson.BsonBinary;
import org.bson.BsonBinarySubType;
import org.bson.Document;
import org.bson.UuidRepresentation;
import org.bson.internal.UuidHelper;
import org.bson.types.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootTest(properties = {"spring.mongodb.uuid.migrate=true"})
class UuidMigrationRunnerIT {

  @Autowired private MongoTemplate mongoTemplate;
  @Autowired private UuidMigrationRunner uuidMigrationRunner;

  @BeforeEach
  void clean() {
    mongoTemplate.getDb().drop();
  }

  @Test
  void shouldMigrateLegacyUuidInDatabase() {

    UUID uuid = UUID.randomUUID();

    byte[] legacyBytes = UuidHelper.encodeUuidToBinary(uuid, UuidRepresentation.JAVA_LEGACY);

    Binary legacyBinary = new Binary(BsonBinarySubType.UUID_LEGACY.getValue(), legacyBytes);

    Document document = new Document("_id", 1).append("uuidField", legacyBinary);

    mongoTemplate.getCollection("test").insertOne(document);

    uuidMigrationRunner.run(new DefaultApplicationArguments());

    Document migrated = mongoTemplate.getCollection("test").find(Filters.eq("_id", 1)).first();

    assertThat(migrated).isNotNull();
    assertThat(migrated.get("uuidField")).isInstanceOf(Binary.class);

    Binary binary = (Binary) migrated.get("uuidField");

    assertThat(binary.getType()).isEqualTo(BsonBinarySubType.UUID_STANDARD.getValue());

    UUID decoded =
        new BsonBinary(binary.getType(), binary.getData()).asUuid(UuidRepresentation.STANDARD);
    assertThat(decoded).isEqualTo(uuid);
  }
}
