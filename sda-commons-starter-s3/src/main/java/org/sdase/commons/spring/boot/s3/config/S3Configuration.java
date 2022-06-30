package org.sdase.commons.spring.boot.s3.config;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Configuration {

  @Value("${s3.accessKey}")
  private String accessKeyId;

  @Value("${s3.secretKey}")
  private String secretKey;

  @Value("${s3.endpoint}")
  private String endpoint;

  @Value("${s3.region}")
  private String region;

  @Bean
  public AmazonS3 getAmazonS3Client() {
    final AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);

    return AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
        .withPathStyleAccessEnabled(true)
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .build();
  }
}