/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SetSystemProperty(key = "management.health.kafka.enabled", value = "true")
@SetSystemProperty(key = "management.health.kafka.timeout", value = "8s")
@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
      "auth.disable=true",
      "opa.disable=true",
      "management.server.port=8071"
    })
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {"listeners=PLAINTEXT://localhost:0", "port=0"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class KafkaHealthIndicatorIntegrationTest {

  @LocalManagementPort private int managementPort;

  @Autowired private TestRestTemplate client;

  @SpyBean private KafkaHealthIndicator kafkaHealthIndicator;

  @Autowired private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  @Test
  void checkThatKafkaHealthCheckIsEnabledAndUp() {
    // given
    // when
    var response = getHealthCheckInfoData();
    var kafkaInfo = response.getBody().components().kafka();

    // then
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody().status()).isEqualTo("UP");
    assertThat(kafkaInfo.status()).isEqualTo("UP");
    assertThat(kafkaInfo.details().info()).isEqualTo("Kafka health check operation succeeded");
  }

  @Test
  void checkThatKafkaHealthCheckIsEnabledAndUpAndTheDown() {
    // given
    // when
    var response = getHealthCheckInfoData();
    var kafkaInfo = response.getBody().components().kafka();

    // then
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody().status()).isEqualTo("UP");
    assertThat(kafkaInfo.status()).isEqualTo("UP");
    assertThat(kafkaInfo.details().info()).isEqualTo("Kafka health check operation succeeded");

    // In case of fatal consumer exception KafkaMessageListenerContainer is executing emergency
    // stop which will call stopAbnormally method
    kafkaListenerEndpointRegistry
        .getListenerContainers()
        .forEach(container -> container.stopAbnormally(() -> {}));

    var response2 = getHealthCheckInfoData();
    var kafkaInfo2 = response2.getBody().components().kafka();

    // then
    assertThat(response2.getStatusCode().is2xxSuccessful()).isFalse();
    assertThat(response2.getBody().status()).isEqualTo("DOWN");
    assertThat(kafkaInfo2.status()).isEqualTo("DOWN");
  }

  @Test
  void checkThatKafkaHealthCheckIsEnabledAndDown() throws Exception {
    // given
    doThrow(new Exception("Simulate kafka health check issue"))
        .when(kafkaHealthIndicator)
        .doHealthCheck(any(Health.Builder.class));

    // when
    var response = getHealthCheckInfoData();
    var kafkaInfo = response.getBody().components().kafka();

    // then
    assertThat(response.getStatusCode().is5xxServerError()).isTrue();
    assertThat(response.getBody().status()).isEqualTo("DOWN");
    assertThat(kafkaInfo.status()).isEqualTo("DOWN");
    assertThat(kafkaInfo.details().error())
        .isEqualTo("java.lang.Exception: Simulate kafka health check issue");
  }

  @Test
  @SetSystemProperty(key = "management.health.kafka.enabled", value = "false")
  @ClearSystemProperty(key = "management.health.kafka.timeout")
  void checkThatKafkaHealthCheckIsNotEnabled() {
    // given
    // when
    var response = getHealthCheckInfoData();
    var kafkaInfo = response.getBody().components().kafka();

    // then
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody().status()).isEqualTo("UP");
    assertThat(kafkaInfo).isNull();
  }

  private ResponseEntity<HealthInfo> getHealthCheckInfoData() {
    return client.getForEntity(
        String.format("http://localhost:%d/healthcheck", managementPort), HealthInfo.class);
  }

  record HealthInfo(String status, Components components) {
    record Components(KafkaInfo kafka) {
      record KafkaInfo(String status, KafkaInfoDetails details) {
        record KafkaInfoDetails(String info, String error) {}
      }
    }
  }
}
