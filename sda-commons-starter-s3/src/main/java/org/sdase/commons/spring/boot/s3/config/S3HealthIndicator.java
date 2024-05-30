/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.health.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;

@Component("s3")
@ConditionalOnEnabledHealthIndicator("s3")
public class S3HealthIndicator extends AbstractHealthIndicator {

  private static final Logger LOGGER = LoggerFactory.getLogger(S3HealthIndicator.class);

  private final S3Client s3Client;
  private final String bucketName;

  public S3HealthIndicator(
      S3Client getAmazonS3Client, @Value("${s3.bucketName}") String bucketName) {
    this.s3Client = getAmazonS3Client;
    this.bucketName = bucketName;
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) {
    try {
      s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
      builder.up().withDetails(Map.of("info", "S3 Bucket Available")).build();
    } catch (Exception e) {
      LOGGER.warn("S3 health check failed to get info of bucket {}", bucketName, e);
      builder
          .down(e)
          .withDetails(Map.of("error", "S3 Bucket Not Available: " + e.getMessage()))
          .build();
    }
  }
}
