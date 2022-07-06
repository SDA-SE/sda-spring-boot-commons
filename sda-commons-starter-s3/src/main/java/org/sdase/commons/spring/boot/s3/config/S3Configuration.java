package org.sdase.commons.spring.boot.s3.config;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AutoConfiguration
public class S3Configuration implements S3BucketOperations{

  private static final Logger LOG = LoggerFactory.getLogger(S3BucketOperations.class);

  @Value("${s3.accessKey}")
  private String accessKeyId;

  @Value("${s3.secretKey}")
  private String secretKey;

  @Value("${s3.endpoint}")
  private String endpoint;

  @Value("${s3.region}")
  private String region;

  @Value("${s3.bucketName}")
  String bucketName;

  @Autowired
  private AmazonS3 amazonS3;

  @Bean
  public AmazonS3 getAmazonS3Client() {
    final AWSCredentials credentials = new BasicAWSCredentials(accessKeyId, secretKey);

    return AmazonS3ClientBuilder.standard()
        .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region))
        .withPathStyleAccessEnabled(true)
        .withCredentials(new AWSStaticCredentialsProvider(credentials))
        .build();
  }


  @Override
  public byte[] findByName(String fileName) {
    byte[] content;
    try {
      S3Object file = amazonS3.getObject(bucketName, fileName);
      S3ObjectInputStream stream = file.getObjectContent();
      content = IOUtils.toByteArray(stream);
    } catch (SdkClientException | IOException clientException) {
      LOG.error(clientException.getMessage());
      return null;
    }
    return content;
  }

  @Override
  public void save(MultipartFile multipartFile) {
    try {
      byte[] fileData = multipartFile.getBytes();
      try (ByteArrayInputStream inputStream = new ByteArrayInputStream(fileData)) {
        final String fileName = LocalDateTime.now() + "_" + multipartFile.getOriginalFilename();
        LOG.info("Uploading file with name {}", fileName);
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(fileData.length);
        final PutObjectRequest putObjectRequest =
            new PutObjectRequest(bucketName, fileName, inputStream, objectMetadata);
        amazonS3.putObject(putObjectRequest);
      }
    } catch (AmazonServiceException | IOException e) {
      LOG.error("Error {} occurred while uploading file", e.getLocalizedMessage());
    }
  }

  @Override
  public List<String> listings() {
    List<String> result = new ArrayList<>();
    ObjectListing listing = amazonS3.listObjects(bucketName);
    List<S3ObjectSummary> summaries = listing.getObjectSummaries();

    for (S3ObjectSummary summary : summaries) {
      result.add(summary.getKey());
    }
    return result;
  }

  @Override
  public void deleteFile(String keyName) {
    LOG.info("Deleting file with name= " + keyName);
    final DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, keyName);
    amazonS3.deleteObject(deleteObjectRequest);
    LOG.info("File deleted successfully.");
  }
}