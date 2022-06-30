package org.sdase.commons.spring.boot.mongodb.health;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class MongoHealthCheck implements HealthIndicator {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoHealthCheck.class);
  private static final Document PING = new Document("ping", 1);
  private final MongoDatabase db;

  public MongoHealthCheck(MongoDatabase db) {
    this.db = db;
  }

  @Override
  public Health health(){
    try{
      Document result = db.runCommand(PING);
      int ok = 0;
      if(result.containsKey("ok") && result.get("ok") instanceof Number) {
        ok = result.get("ok", Number.class).intValue();
      }
      if (ok != 1){
        LOGGER.warn("Unexpected ping response: {}", result);
        return Health.down().build();
      }
      return Health.up().build();

    } catch (Exception e){
      LOGGER.warn("Failed health check", e);
    }
    return Health.down().build();
  }

}

