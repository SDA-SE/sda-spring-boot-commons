package org.sdase.commons.spring.boot.s3.Service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class FileService {

  private static final Logger LOG = LoggerFactory.getLogger(FileService.class);

  private AmazonS3 amazonS3;

  private String bucketName;

  public FileService(AmazonS3 amazonS3, @Value("${s3bucketName}") String bucketName) {
    this.amazonS3 = amazonS3;
    this.bucketName = bucketName;
  }

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

  public List<String> listings() {
    List<String> result = new ArrayList<>();
    ObjectListing listing = amazonS3.listObjects(bucketName);
    List<S3ObjectSummary> summaries = listing.getObjectSummaries();

    for (S3ObjectSummary summary : summaries) {
      result.add(summary.getKey());
    }
    return result;
  }

  public void deleteFile(final String keyName) {
    LOG.info("Deleting file with name= " + keyName);
    final DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, keyName);
    amazonS3.deleteObject(deleteObjectRequest);
    LOG.info("File deleted successfully.");
  }
}
