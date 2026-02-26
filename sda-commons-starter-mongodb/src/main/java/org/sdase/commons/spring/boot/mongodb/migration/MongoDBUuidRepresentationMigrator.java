/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mongodb.migration;

import static org.bson.BsonBinarySubType.UUID_LEGACY;
import static org.bson.BsonBinarySubType.UUID_STANDARD;
import static org.bson.UuidRepresentation.JAVA_LEGACY;
import static org.bson.UuidRepresentation.STANDARD;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import org.bson.BsonBinary;
import org.bson.Document;
import org.bson.internal.UuidHelper;
import org.bson.types.Binary;

// based of
// https://gist.github.com/gavvvr/4e01f2d26b42c71e50892be0fee9de8e#file-mongodbuuidrepresentationmigrator-java
/**
 * Utility class for migrating UUID values in {@link Document} instances from the legacy MongoDB
 * UUID representation to the standard representation.
 *
 * <p>This migration is required whenever UUID fields in your MongoDB documents use the legacy
 * {@link org.bson.UuidRepresentation#JAVA_LEGACY} representation but your application expects the
 * standard {@link org.bson.UuidRepresentation#STANDARD} representation. Failing to perform this
 * migration can lead to deserialization errors or inconsistent UUID values.
 *
 * <p>This class recursively scans a {@link Document} and all embedded documents for fields of type
 * {@link org.bson.types.Binary} with the legacy UUID subtype and converts them to the standard
 * subtype.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Document doc = mongoTemplate.findOne(query, Document.class, "myCollection");
 * boolean changed = MongoDBUuidRepresentationMigrator.convertUuidValuesFromLegacyToStandardFor(doc);
 * if (changed) {
 *     mongoTemplate.save(doc, "myCollection");
 * }
 * }</pre>
 */
public class MongoDBUuidRepresentationMigrator {

  private MongoDBUuidRepresentationMigrator() {}

  /**
   * Converts all UUID fields in the given {@link Document} (including nested documents) from the
   * legacy UUID representation to the standard representation.
   *
   * @param rootDocument the root document to process
   * @return {@code true} if at least one UUID field was converted, {@code false} otherwise
   */
  public static boolean convertUuidValuesFromLegacyToStandardFor(Document rootDocument) {
    boolean docWasChanged = false;
    Queue<Document> queue = new LinkedList<>();
    queue.offer(rootDocument);

    while (!queue.isEmpty()) {
      Document document = queue.remove();
      for (Map.Entry<String, Object> kv : document.entrySet()) {
        String fieldName = kv.getKey();
        Object fieldValue = kv.getValue();
        if (fieldValue instanceof Binary binaryField
            && binaryField.getType() == UUID_LEGACY.getValue()) {
          migrateUuidRepresentationFromLegacyToStandard(document, fieldName);
          docWasChanged = true;
        } else if (fieldValue instanceof Document fieldDoc) {
          queue.add(fieldDoc);
        }
      }
    }
    return docWasChanged;
  }

  private static void migrateUuidRepresentationFromLegacyToStandard(Document document, String key) {
    Binary legacyBinary = (Binary) document.get(key);
    UUID uuid = new BsonBinary(legacyBinary.getType(), legacyBinary.getData()).asUuid(JAVA_LEGACY);
    document.put(key, new BsonBinary(UUID_STANDARD, UuidHelper.encodeUuidToBinary(uuid, STANDARD)));
  }
}
