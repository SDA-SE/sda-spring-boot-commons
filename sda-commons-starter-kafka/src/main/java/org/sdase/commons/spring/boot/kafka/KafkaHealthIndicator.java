/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DescribeClusterOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.stereotype.Component;

/**
 * Simple implementation of a HealthIndicator returning kafka cluster identifier and number of kafka
 * nodes.
 */
@Component("kafka")
@ConditionalOnEnabledHealthIndicator("kafka")
public class KafkaHealthIndicator extends AbstractHealthIndicator {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaHealthIndicator.class);

  private final AdminClient kafkaAdminClient;
  private final Duration kafkaCommandTimeout;

  public KafkaHealthIndicator(
      @Value("${management.health.kafka.timeout:4s}") Duration kafkaCommandTimeout,
      KafkaAdmin kafkaAdmin) {
    super("Kafka health check failed");

    this.kafkaCommandTimeout = kafkaCommandTimeout;
    this.kafkaAdminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties());

    LOG.info("Kafka health check is initialized with timeout duration {}", kafkaCommandTimeout);
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) throws Exception {
    var clusterInfo =
        kafkaAdminClient.describeCluster(
            new DescribeClusterOptions()
                .timeoutMs((int) TimeUnit.MILLISECONDS.convert(kafkaCommandTimeout)));

    builder
        .up()
        .withDetails(
            Map.of(
                "clusterId",
                clusterInfo.clusterId().get(),
                "nodeCount",
                clusterInfo.nodes().get().size()))
        .build();
  }
}
