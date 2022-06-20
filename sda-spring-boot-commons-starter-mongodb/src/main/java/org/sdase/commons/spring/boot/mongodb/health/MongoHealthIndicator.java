package org.sdase.commons.spring.boot.mongodb.health;

import org.bson.Document;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * implementation health indicator returning status information for mongo data stores
 */

@Component
public class MongoHealthIndicator extends AbstractHealthIndicator {

  private MongoTemplate mongoTemplate;

  public MongoHealthIndicator(MongoTemplate mongoTemplate) {
    super("MongoDb health check failed");
    Assert.notNull(mongoTemplate, "MongoTemplate must not be null");
    this.mongoTemplate = mongoTemplate;
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) throws Exception {
    Document result = mongoTemplate.executeCommand("{ buildInfo: 1 }");
    builder.up().withDetail("version", result.getString("version"));

  }
}
