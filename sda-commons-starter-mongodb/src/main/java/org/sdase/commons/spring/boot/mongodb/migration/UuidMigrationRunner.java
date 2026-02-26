/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mongodb.migration;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.BulkWriteOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOneModel;
import com.mongodb.client.model.WriteModel;
import java.util.ArrayList;
import java.util.List;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

/**
 * Spring {@link org.springframework.boot.ApplicationRunner} implementation for migrating all UUID
 * fields in MongoDB collections from the legacy UUID representation to the standard one.
 *
 * <p>This runner will only execute if the property {@code spring.mongo.uuid.migrate} is set to
 * {@code true}. It iterates over all collections in the database, scans all documents in batches,
 * and performs a bulk update for documents containing legacy UUIDs.
 *
 * <p>This class should be used whenever the UUID representation in MongoDB differs from what the
 * application expects. Existing UUID fields must be migrated to avoid deserialization errors, data
 * inconsistencies, or serialization issues.
 *
 * <p>Example Spring Boot configuration:
 *
 * <pre>{@code
 * spring.mongo.uuid.migrate=true
 * }</pre>
 */
@Component
@ConditionalOnBooleanProperty("spring.mongodb.uuid.migrate")
public class UuidMigrationRunner implements ApplicationRunner {

  private static final Logger LOGGER = LoggerFactory.getLogger(UuidMigrationRunner.class);

  private static final int BATCH_SIZE = 500;

  private final MongoTemplate mongoTemplate;

  /**
   * Creates a new UUID migration runner.
   *
   * @param mongoTemplate the {@link MongoTemplate} to use for accessing MongoDB collections
   */
  public UuidMigrationRunner(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  /**
   * Executes the migration on application startup if enabled.
   *
   * @param args the application arguments
   */
  @Override
  public void run(ApplicationArguments args) {
    LOGGER.info("Starting UUID legacy → standard migration");

    for (String collectionName : mongoTemplate.getCollectionNames()) {
      migrateCollection(mongoTemplate.getCollection(collectionName));
    }

    LOGGER.info("UUID migration completed");
  }

  private void migrateCollection(MongoCollection<Document> collection) {

    long start = System.currentTimeMillis();
    long scanned = 0;
    long updated = 0;
    int batchCounter = 0;

    LOGGER.info("Migrating collection: {}", collection.getNamespace().getCollectionName());

    List<WriteModel<Document>> bulkOperations = new ArrayList<>();

    try (MongoCursor<Document> cursor = collection.find().batchSize(BATCH_SIZE).iterator()) {

      while (cursor.hasNext()) {
        Document document = cursor.next();
        scanned++;

        boolean changed =
            MongoDBUuidRepresentationMigrator.convertUuidValuesFromLegacyToStandardFor(document);

        if (changed) {
          Object id = document.get("_id");
          if (id != null) {
            bulkOperations.add(new ReplaceOneModel<>(Filters.eq("_id", id), document));
            updated++;
          }
        }

        if (bulkOperations.size() == BATCH_SIZE) {
          executeBulk(collection, bulkOperations, ++batchCounter);
        }
      }

      if (!bulkOperations.isEmpty()) {
        executeBulk(collection, bulkOperations, ++batchCounter);
      }
    }

    long duration = System.currentTimeMillis() - start;

    LOGGER.info(
        "Finished collection {} | scanned={} updated={} duration={}ms",
        collection.getNamespace().getCollectionName(),
        scanned,
        updated,
        duration);
  }

  private void executeBulk(
      MongoCollection<Document> collection,
      List<WriteModel<Document>> bulkOperations,
      int batchNumber) {

    LOGGER.debug("Executing bulk batch {} ({} operations)", batchNumber, bulkOperations.size());

    collection.bulkWrite(bulkOperations, new BulkWriteOptions().ordered(false));

    bulkOperations.clear();
  }
}
