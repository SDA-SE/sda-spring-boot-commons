/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.endpoints.S3EndpointProvider;
import software.amazon.awssdk.utils.StringUtils;

@AutoConfiguration
public class S3Configuration {

  private final String accessKeyId;
  private final String secretKey;
  private final String endpoint;
  private final String region;
  private final String bucketName;

  public S3Configuration(
      @Value("${s3.accessKey}") String accessKeyId,
      @Value("${s3.secretKey}") String secretKey,
      @Value("${s3.endpoint}") String endpoint,
      @Value("${s3.region}") String region,
      @Value("${s3.bucketName}") String bucketName) {
    this.accessKeyId = accessKeyId;
    this.secretKey = secretKey;
    this.endpoint = endpoint;
    this.region = region;
    this.bucketName = bucketName;
  }

  @Bean
  @ConditionalOnMissingBean
  public S3Client getAmazonS3Client() {
    Region s3EndpointRegion = Region.of(region);
    return S3Client.builder()
        .region(s3EndpointRegion)
        .credentialsProvider(
            (StringUtils.isBlank(accessKeyId) && StringUtils.isBlank(secretKey))
                ? AnonymousCredentialsProvider.create()
                : StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretKey)))
        .endpointProvider(S3EndpointProvider.defaultProvider())
        .endpointOverride(URI.create(endpoint))
        .forcePathStyle(true)
        .build();
  }

  @Bean
  @ConditionalOnMissingBean
  public S3BucketRepository s3BucketRepository(S3Client getAmazonS3Client) {
    return new S3BucketRepository(getAmazonS3Client, bucketName);
  }
}
