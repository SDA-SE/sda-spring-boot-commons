/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.s3.config;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3BucketRepository {

  private static final Logger LOG = LoggerFactory.getLogger(S3BucketRepository.class);

  String bucketName;
  private AmazonS3 amazonS3;

  public S3BucketRepository(AmazonS3 amazonS3, String bucketName) {
    this.amazonS3 = amazonS3;
    this.bucketName = bucketName;
  }

  /**
   * This methods uses the keyName to access the S3bucket and returns the content. Three exceptions
   * are handled here. The first case is when the object does not exist at all for the given
   * keyName. Throws SdkClientException. Secondly step, it gets file content and checks if file is
   * null (throws nullPointerException). Logs a message when it is an empty file (no content).
   * Finally, it returns a byte Array which holds the content of the file.
   *
   * @param keyName the keyName of the s3Bucket
   * @return the content of the object in the S3 bucket with the given keyName.
   */
  public byte[] findByName(String keyName) {
    byte[] content = null;
    S3Object file = null;
    try {
      file = amazonS3.getObject(bucketName, keyName);
    } catch (SdkClientException e) {
      LOG.error("No object exists with the given keyName={}", keyName);
    }
    try {
      S3ObjectInputStream stream = file.getObjectContent();
      if (stream.equals(null)) {
        throw new NullPointerException("File is null");
      }
      content = IOUtils.toByteArray(stream);
      if (content.length == 0) {
        LOG.info("File is empty");
      }
    } catch (IOException clientException) {
      LOG.error(clientException.getMessage());
    }
    return content;
  }

  /**
   * uploads file to s3 bucket (bucketName) by passing the pathname to the file, and the key in
   * which the file is to be uploaded to. Throws FileNotFoundException if the file cannot be found.
   * Checks if path exists and if the file is a normal file. Throws an exception if the directory/
   * file
   *
   * @param key The key under which to store the specified file
   * @param file The file containing the data to be uploaded to Amazon S3.
   */
  public void saveFile(String key, File file) throws FileNotFoundException {
    if (!file.exists()) {
      throw new FileNotFoundException(
          String.format("No file exists in the provided path %s ", file));
    }
    if (!file.isFile()) {
      throw new FileNotFoundException(
          String.format("The file denoted by the Pathname %s is not a normal file)", file));
    }
    LOG.info("Uploading file with name {}", file.getName());
    amazonS3.putObject(bucketName, key, file);
    LOG.info(
        "Done...    File with name {} has been uploaded to the {}", file.getName(), bucketName);
  }

  /**
   * This method provides a summary of all the objects existing in the s3 bucket. The summary of
   * each object is saved in a list as a collection of Strings
   *
   * @return the list containing the bucket (instance variable) objects.
   */
  public List<String> listings() {
    ObjectListing listing = amazonS3.listObjects(bucketName);
    return listing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).toList();
  }

  /**
   * This method receives as parameter the keyName of an S3Object and deletes this object from the
   * s3 bucket. Throws AmazonS3Exception with customized message if keyName does not exist. Deletes
   * the object if keyName exist and logs the message "File deleted successfully"
   *
   * @param keyName the key of the file to be deleted in s3 bucket
   */
  public void deleteFile(final String keyName) {
    try {
      LOG.info("Deleting file with key={}", keyName);
      amazonS3.deleteObject(bucketName, keyName);
      LOG.info("File deleted successfully.");
    } catch (AmazonS3Exception e) {
      LOG.error("No object with keyName={} was found in the {} bucket", keyName, bucketName);
    }
  }

  /**
   * This method verifies if an object exists in the s3 bucket or not
   *
   * @param objectName the name of the object
   * @return true if object exist and false if it doesn't
   */
  public boolean doesObjectExist(String objectName) {
    return amazonS3.doesObjectExist(bucketName, objectName);
  }
}
