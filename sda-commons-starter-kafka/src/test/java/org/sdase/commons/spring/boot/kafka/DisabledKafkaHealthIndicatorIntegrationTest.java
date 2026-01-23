/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;

@SetSystemProperty(key = "management.health.kafka.enabled", value = "false")
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
    brokerProperties = {"port=0"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@AutoConfigureTestRestTemplate
class DisabledKafkaHealthIndicatorIntegrationTest {

  @LocalManagementPort private int managementPort;

  @Autowired private TestRestTemplate client;

  @Autowired private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

  @Test
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
