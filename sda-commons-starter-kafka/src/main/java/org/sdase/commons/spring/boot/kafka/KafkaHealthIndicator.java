/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.endpoint.OperationResponseBody;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.stereotype.Component;

/**
 * Simple implementation of a HealthIndicator returning kafka cluster identifier and number of kafka
 * nodes.
 */
@Component("kafka")
@ConditionalOnEnabledHealthIndicator("kafka")
public class KafkaHealthIndicator extends AbstractHealthIndicator implements OperationResponseBody {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaHealthIndicator.class);

  private final AdminClient kafkaAdminClient;
  private final Duration kafkaCommandTimeout;

  private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  public KafkaHealthIndicator(
      @Value("${management.health.kafka.timeout:4s}") Duration kafkaCommandTimeout,
      KafkaAdmin kafkaAdmin,
      KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry) {
    super("Kafka health check operation failed");

    this.kafkaCommandTimeout = kafkaCommandTimeout;
    this.kafkaAdminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());
    this.kafkaListenerEndpointRegistry = kafkaListenerEndpointRegistry;

    LOG.info("Kafka health check is initialized with timeout duration {}", kafkaCommandTimeout);
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) throws Exception {

    if (!isAnyMessageListenerContainersRunning()) {
      builder.down().withDetails(Map.of("error", "No MessageListenerContainers running")).build();
      return;
    }

    kafkaAdminClient
        .listTopics(
            new ListTopicsOptions()
                .timeoutMs((int) TimeUnit.MILLISECONDS.convert(kafkaCommandTimeout)))
        .names()
        .get();

    builder.up().withDetails(Map.of("info", "Kafka health check operation succeeded")).build();
  }

  private boolean isAnyMessageListenerContainersRunning() {

    Collection<MessageListenerContainer> listenerContainers =
        kafkaListenerEndpointRegistry.getListenerContainers();

    //    only producers are used
    if (listenerContainers.isEmpty()) {
      return true;
    }

    return listenerContainers.stream().anyMatch(MessageListenerContainer::isRunning);
  }
}
