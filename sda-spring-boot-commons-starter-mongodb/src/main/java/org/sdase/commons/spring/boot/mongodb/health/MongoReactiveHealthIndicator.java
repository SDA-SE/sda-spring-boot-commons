package org.sdase.commons.spring.boot.mongodb.health;

import org.bson.Document;
import org.springframework.boot.actuate.health.AbstractReactiveHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;

@Component
public class MongoReactiveHealthIndicator extends AbstractReactiveHealthIndicator {

  private final ReactiveMongoTemplate reactiveMongoTemplate;

  public MongoReactiveHealthIndicator(ReactiveMongoTemplate reactiveMongoTemplate) {
    super("Mongo health check failed");
    Assert.notNull(reactiveMongoTemplate, "ReactiveMongoTemplate must not be null");
    this.reactiveMongoTemplate = reactiveMongoTemplate;
  }

  @Override
  protected Mono<Health> doHealthCheck(Health.Builder builder) {
    Mono<Document> buildInfo = reactiveMongoTemplate.executeCommand("{ buildInfo: 1}");
    return buildInfo.map((document) -> up(builder, document));
  }

  private Health up(Health.Builder builder, Document document){
    return builder.up().withDetail("version", document.getString("version")).build();
  }
}
