/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.amazonaws.util.IOUtils;
import java.io.*;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class S3BucketRepository {

  private static final Logger LOG = LoggerFactory.getLogger(S3BucketRepository.class);
  private final String bucketName;
  private final AmazonS3 amazonS3;

  public S3BucketRepository(AmazonS3 amazonS3, String bucketName) {
    this.amazonS3 = amazonS3;
    this.bucketName = bucketName;
  }

  /**
   * Gets the object stored in Amazon S3 under the configured bucket and passed key.
   *
   * @param key The key under which the desired object is stored.
   * @return the content of the object as byte array in the S3 bucket with the given key.
   * @throws SdkClientException If any errors are encountered in the s3 client while making the
   *     request or handling the response.
   * @throws AmazonServiceException If any errors occurred in Amazon S3 while processing the
   *     request.
   * @throws IOException if the object content could not be provided as byte array.
   */
  public byte[] findByName(String key) throws IOException {
    var file = amazonS3.getObject(bucketName, key);
    var s3ObjectInputStream = file.getObjectContent();
    return IOUtils.toByteArray(s3ObjectInputStream);
  }

  /**
   * Uploads file to s3 bucket to the configured bucket name. By passing the file and the key in
   * which the file is to be uploaded to.
   *
   * @param key The key under which to store the specified file
   * @param file The file containing the data to be uploaded to Amazon S3.
   * @throws SdkClientException If any errors are encountered in the s3 client while making the
   *     request or handling the response.
   * @throws AmazonServiceException If any errors occurred in Amazon S3 while processing the
   *     request.
   * @throws IOException if the passed file does not exist nor is a file to upload.
   */
  public void saveFile(String key, File file) throws IOException {
    if (!file.exists()) {
      throw new FileNotFoundException(
          String.format("No file exists in the provided path %s ", file));
    }
    if (!file.isFile()) {
      throw new IOException(String.format("The path '%s' is not a file)", file.getAbsolutePath()));
    }
    LOG.info("Uploading file with name {}...", file.getName());
    amazonS3.putObject(bucketName, key, file);
    LOG.info("Done! File with name '{}' has been uploaded to the '{}'", file.getName(), bucketName);
  }

  /**
   * Uploads data as string to s3 bucket to the configured bucket name. By passing the content and
   * the key in which the file is to be uploaded to.
   *
   * @param key The key under which to store the specified content
   * @param content The data to be uploaded to Amazon S3. Will be saved as UTF-8.
   * @throws IOException If the content is null
   * @throws SdkClientException If any errors are encountered in the s3 client while making the
   *     request or handling the response.
   * @throws AmazonServiceException If any errors occurred in Amazon S3 while processing the
   *     request.
   */
  public void save(String key, String content) throws IOException {
    if (content == null) {
      throw new IOException(String.format("Content to be saved by key %s must not be null!", key));
    }

    LOG.info("Uploading data with key {}...", key);
    amazonS3.putObject(bucketName, key, content);
    LOG.info("Done! Data with key '{}' has been uploaded to the '{}'", key, bucketName);
  }

  /**
   * This method provides a list of all the objects stored in the s3 bucket. The {@linkplain
   * S3ObjectSummary#getKey() keys} are resolved by the {@linkplain S3ObjectSummary}.
   *
   * @return the list of keys of every file in the bucket.
   */
  public List<String> listings() {
    ObjectListing listing = amazonS3.listObjects(bucketName);
    return listing.getObjectSummaries().stream().map(S3ObjectSummary::getKey).toList();
  }

  /**
   * Deletes the specified object in the configured bucket. Successful when the object is deleted or
   * the object does not exist.
   *
   * @param key the key of the file to be deleted in s3 bucket
   * @throws SdkClientException If any errors are encountered in the s3 client while making the
   *     request or handling the response.
   * @throws AmazonServiceException If any errors occurred in Amazon S3 while processing the
   *     request.
   */
  public void deleteFile(String key) {
    LOG.info("Deleting file with key='{}' ...", key);
    amazonS3.deleteObject(bucketName, key);
    LOG.info("Done! File with key='{}' deleted successfully!", key);
  }

  /**
   * This method verifies if an object exists in the s3 bucket or not
   *
   * @param key the key of the object
   * @return true, if object exist.
   * @throws AmazonServiceException If any errors occurred in Amazon S3 while processing the
   *     request.
   * @throws SdkClientException If any errors are encountered in the s3 client while making the
   *     request or handling the response.
   */
  public boolean doesObjectExist(String key) {
    return amazonS3.doesObjectExist(bucketName, key);
  }
}
