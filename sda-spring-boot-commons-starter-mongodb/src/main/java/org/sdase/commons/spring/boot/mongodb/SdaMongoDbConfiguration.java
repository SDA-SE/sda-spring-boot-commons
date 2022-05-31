/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.mongodb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;
import org.springframework.data.mongodb.core.index.IndexResolver;
import org.springframework.data.mongodb.core.index.MongoPersistentEntityIndexResolver;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Enables features that make a Spring Boot service using MongoDB ready to use within the SDA
 * platform.
 *
 * <p>So far this covers:
 *
 * <ul>
 *   <li>{@linkplain org.springframework.core.convert.converter.Converter Converter} for {@linkplain
 *       java.time.ZonedDateTime}
 * </ul>
 *
 * <p>Not covered:
 *
 * <ul>
 *   <li>Mongo Client Options are only configurable via connection uri
 *   <li>CA certificate can't be configured via environment variable
 * </ul>
 */
@Configuration
@PropertySource("classpath:/org/sdase/commons/spring/boot/mongodb/defaults.properties")
public class SdaMongoDbConfiguration {

  private static final Logger LOG = LoggerFactory.getLogger(SdaMongoDbConfiguration.class);

  private final MongoTemplate mongoTemplate;

  public SdaMongoDbConfiguration(MongoTemplate mongoTemplate) {
    this.mongoTemplate = mongoTemplate;
  }

  /**
   * Since it's not recommended to automatically create indices via {@code
   * spring.data.mongodb.auto-index-creation=true}. Have a look here: <a
   * href="https://github.com/spring-projects/spring-data-mongodb/issues/3049">https://github.com/spring-projects/spring-data-mongodb/issues/3049</a>
   */
  @EventListener(ContextRefreshedEvent.class)
  public void initIndicesAfterStartup() {

    var mappingContext = mongoTemplate.getConverter().getMappingContext();

    IndexResolver resolver = new MongoPersistentEntityIndexResolver(mappingContext);

    // consider only entities that are annotated with @Document
    mappingContext.getPersistentEntities().stream()
        .filter(it -> it.isAnnotationPresent(Document.class))
        .forEach(
            it -> {
              IndexOperations indexOps = mongoTemplate.indexOps(it.getType());
              resolver
                  .resolveIndexFor(it.getType())
                  .forEach(
                      indexDefinition -> {
                        indexOps.ensureIndex(indexDefinition);
                        LOG.info("Ensured index '{}'", indexDefinition.getIndexOptions());
                      });
            });
  }
}
