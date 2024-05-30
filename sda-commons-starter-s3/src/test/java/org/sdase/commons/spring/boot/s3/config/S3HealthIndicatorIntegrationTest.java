/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.sdase.commons.spring.boot.s3.S3TestApp;
import org.sdase.commons.spring.boot.web.testing.s3.S3Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalManagementPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@SetSystemProperty(key = "management.health.s3.enabled", value = "true")
@SpringBootTest(
    classes = S3TestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties = {"auth.disable=true", "opa.disable=true", "management.server.port=8071"})
@S3Test
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class S3HealthIndicatorIntegrationTest {

  @LocalManagementPort private int managementPort;

  @Autowired private TestRestTemplate client;

  @SpyBean private S3Client s3Client;

  @Test
  void checkThatS3HealthCheckIsEnabledAndUp() {
    var response = getHealthCheckInfoData();
    var s3Info = response.getBody().components().s3();
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody().status()).isEqualTo("UP");
    assertThat(s3Info.status()).isEqualTo("UP");
    assertThat(s3Info.details().info()).isEqualTo("S3 Bucket Available");
  }

  @Test
  void checkThatS3HealthCheckIsEnabledAndDown() {
    doThrow(new RuntimeException("Simulate s3 health check issue"))
        .when(s3Client)
        .headBucket(any(HeadBucketRequest.class));
    var response = getHealthCheckInfoData();
    var s3Info = response.getBody().components().s3();
    assertThat(response.getStatusCode().is5xxServerError()).isTrue();
    assertThat(response.getBody().status()).isEqualTo("DOWN");
    assertThat(s3Info.status()).isEqualTo("DOWN");
    assertThat(s3Info.details().error())
        .isEqualTo("S3 Bucket Not Available: Simulate s3 health check issue");
  }

  @Test
  @SetSystemProperty(key = "management.health.s3.enabled", value = "false")
  void checkThatS3HealthCheckIsNotEnabled() {
    var response = getHealthCheckInfoData();
    var s3Info = response.getBody().components().s3();
    assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    assertThat(response.getBody().status()).isEqualTo("UP");
    assertThat(s3Info).isNull();
  }

  private ResponseEntity<HealthInfo> getHealthCheckInfoData() {
    return client.getForEntity(
        String.format("http://localhost:%d/healthcheck", managementPort), HealthInfo.class);
  }

  record HealthInfo(String status, Components components) {
    record Components(S3Info s3) {
      record S3Info(String status, S3InfoDetails details) {
        record S3InfoDetails(String info, String error) {}
      }
    }
  }
}
