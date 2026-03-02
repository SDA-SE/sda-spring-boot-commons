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

import java.util.*;
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
 * <p>This class recursively scans a {@link Document} and all embedded documents, lists, sets and
 * maps for values of type {@link org.bson.types.Binary} with the legacy UUID subtype and converts
 * them to the standard subtype.
 *
 * <p>The original {@link Document} instance is not modified. Instead, a new {@link Document}
 * instance is returned if any UUID values are converted. If no changes are required, the original
 * instance is returned unchanged.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * Document doc = mongoTemplate.findOne(query, Document.class, "myCollection");
 * Document migrated =
 *     MongoDBUuidRepresentationMigrator.convertUuidValuesFromLegacyToStandardFor(doc);
 *
 * if (migrated != doc) {
 *     mongoTemplate.save(migrated, "myCollection");
 * }
 * }</pre>
 */
public class MongoDBUuidRepresentationMigrator {

  private MongoDBUuidRepresentationMigrator() {}

  /**
   * Recursively converts all UUID values in the given {@link Document} from the legacy
   * representation to the standard representation.
   *
   * <p>The provided document is not mutated. If at least one UUID value is converted, a new {@link
   * Document} instance reflecting the changes is returned. Otherwise, the original document
   * instance is returned.
   *
   * @param rootDocument the root document to process
   * @return either the original document (if no changes were necessary) or a new document
   *     containing the converted UUID values
   */
  public static Document convertUuidValuesFromLegacyToStandardFor(Document rootDocument) {
    return (Document) processValue(rootDocument);
  }

  private static Object processValue(Object value) {

    if (value instanceof Binary binaryField && binaryField.getType() == UUID_LEGACY.getValue()) {
      return migrateUuidRepresentationFromLegacyToStandard(binaryField);
    }

    if (value instanceof Document document) {
      return processDocument(document);
    }

    if (value instanceof List<?> list) {
      return processList(list);
    }

    if (value instanceof Set<?> set) {
      return processSet(set);
    }

    if (value instanceof Map<?, ?> map) {
      return processMap(map);
    }

    return value;
  }

  private static Document processDocument(Document document) {
    boolean changed = false;
    Document rebuilt = new Document();

    for (Map.Entry<String, Object> entry : document.entrySet()) {
      Object original = entry.getValue();
      Object processed = processValue(original);

      rebuilt.put(entry.getKey(), processed);

      if (processed != original) {
        changed = true;
      }
    }

    return changed ? rebuilt : document;
  }

  private static Object processMap(Map<?, ?> map) {
    boolean changed = false;
    Map<Object, Object> rebuilt = new LinkedHashMap<>();

    for (Map.Entry<?, ?> entry : map.entrySet()) {
      Object original = entry.getValue();
      Object processed = processValue(original);

      rebuilt.put(entry.getKey(), processed);

      if (processed != original) {
        changed = true;
      }
    }

    return changed ? Map.copyOf(rebuilt) : map;
  }

  private static Object processList(List<?> list) {
    boolean changed = false;
    List<Object> rebuilt = new ArrayList<>(list.size());

    for (Object element : list) {
      Object processed = processValue(element);
      rebuilt.add(processed);

      if (processed != element) {
        changed = true;
      }
    }

    return changed ? List.copyOf(rebuilt) : list;
  }

  private static Object processSet(Set<?> set) {
    boolean changed = false;
    Set<Object> rebuilt = HashSet.newHashSet(set.size());

    for (Object element : set) {
      Object processed = processValue(element);
      rebuilt.add(processed);

      if (processed != element) {
        changed = true;
      }
    }

    return changed ? Set.copyOf(rebuilt) : set;
  }

  private static BsonBinary migrateUuidRepresentationFromLegacyToStandard(Binary binaryField) {
    UUID uuid = new BsonBinary(binaryField.getType(), binaryField.getData()).asUuid(JAVA_LEGACY);

    return new BsonBinary(UUID_STANDARD, UuidHelper.encodeUuidToBinary(uuid, STANDARD));
  }
}
