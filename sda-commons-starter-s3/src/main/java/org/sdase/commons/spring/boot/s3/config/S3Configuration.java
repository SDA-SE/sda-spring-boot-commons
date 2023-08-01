/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

// import com.amazonaws.auth.AWSCredentials; TODO packages and classes changed
// import com.amazonaws.auth.AWSStaticCredentialsProvider;
// import com.amazonaws.auth.BasicAWSCredentials;
// import com.amazonaws.client.builder.AwsClientBuilder;
// import com.amazonaws.services.s3.AmazonS3;
// import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;

@AutoConfiguration
public class S3Configuration {

  @Value("${s3.accessKey}")
  private String accessKeyId;

  @Value("${s3.secretKey}")
  private String secretKey;

  @Value("${s3.endpoint}")
  private String endpoint;

  @Value("${s3.region}")
  private String region;

  @Value("${s3.bucketName}")
  private String bucketName;

  // TODO API has completely changed, is there good reason to provide the raw client instead of
  // using Spring Cloud S3Operations?
  //  @Bean
  //  @ConditionalOnMissingBean
  //  public AmazonS3 getAmazonS3Client() {
  //    final AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);
  //
  //    return AmazonS3ClientBuilder.standard()
  //        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
  //        .withPathStyleAccessEnabled(true)
  //        .withCredentials(new AWSStaticCredentialsProvider(credentials))
  //        .build();
  //  }

  //  @Bean
  //  @ConditionalOnMissingBean
  //  public S3BucketRepository s3BucketRepository(AmazonS3 getAmazonS3Client) {
  //    return new S3BucketRepository(getAmazonS3Client, bucketName);
  //  }

  String getAccessKeyId() {
    return accessKeyId;
  }

  String getSecretKey() {
    return secretKey;
  }

  String getEndpoint() {
    return endpoint;
  }

  String getRegion() {
    return region;
  }

  String getBucketName() {
    return bucketName;
  }
}
