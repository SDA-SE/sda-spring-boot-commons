/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

import io.awspring.cloud.s3.S3Exception;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.InvalidObjectStateException;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

public class S3BucketRepository {

  private static final Logger LOG = LoggerFactory.getLogger(S3BucketRepository.class);
  private final String bucketName;
  private final S3Client s3Client;

  public S3BucketRepository(S3Client s3Client, String bucketName) {
    this.s3Client = s3Client;
    this.bucketName = bucketName;
  }

  /**
   * Gets the object stored in Amazon S3 under the configured bucket and passed key.
   *
   * @param key The key under which the desired object is stored.
   * @return the content of the object as byte array in the S3 bucket with the given key.
   * @throws NoSuchKeyException The specified key does not exist.
   * @throws InvalidObjectStateException Object is archived and inaccessible until restored.
   * @throws SdkException Base class for all exceptions that can be thrown by the SDK (both service
   *     and client). Can be used for catch all scenarios.
   * @throws SdkClientException If any client side error occurs such as an IO related failure,
   *     failure to get credentials, etc.
   * @throws S3Exception Base class for all service exceptions. Unknown exceptions will be thrown as
   *     an instance of this type.
   */
  public byte[] findByName(String key) {
    var resource =
        s3Client.getObjectAsBytes(GetObjectRequest.builder().bucket(bucketName).key(key).build());
    return resource.asByteArray();
  }

  /**
   * Uploads file to s3 bucket to the configured bucket name. By passing the file and the key in
   * which the file is to be uploaded to.
   *
   * @param key The key under which to store the specified file
   * @param file The file containing the data to be uploaded to Amazon S3.
   * @throws SdkException Base class for all exceptions that can be thrown by the SDK (both service
   *     and client). Can be used for catch all scenarios.
   * @throws SdkClientException If any client side error occurs such as an IO related failure,
   *     failure to get credentials, etc.
   * @throws S3Exception Base class for all service exceptions. Unknown exceptions will be thrown as
   *     an instance of this type.
   */
  public void saveFile(String key, File file) throws S3Exception, SecurityException {
    LOG.info("Uploading file with name {}...", file.getName());
    s3Client.putObject(
        PutObjectRequest.builder().bucket(bucketName).key(key).build(), RequestBody.fromFile(file));
    LOG.info("Done! File with name '{}' has been uploaded to the '{}'", file.getName(), bucketName);
  }

  /**
   * Uploads data as string to s3 bucket to the configured bucket name. By passing the content and
   * the key in which the file is to be uploaded to.
   *
   * @param key The key under which to store the specified content
   * @param content The data to be uploaded to Amazon S3. Will be saved as UTF-8.
   * @throws IOException If the content is null
   * @throws SdkException Base class for all exceptions that can be thrown by the SDK (both service
   *     and client). Can be used for catch all scenarios.
   * @throws SdkClientException If any client side error occurs such as an IO related failure,
   *     failure to get credentials, etc.
   * @throws S3Exception Base class for all service exceptions. Unknown exceptions will be thrown as
   *     an instance of this type.
   */
  public void save(String key, String content) throws IOException {
    if (content == null) {
      throw new IOException(String.format("Content to be saved by key %s must not be null!", key));
    }
    LOG.info("Uploading data with key {}...", key);
    s3Client.putObject(
        PutObjectRequest.builder().bucket(bucketName).key(key).build(),
        RequestBody.fromBytes(content.getBytes(StandardCharsets.UTF_8)));
    LOG.info("Done! Data with key '{}' has been uploaded to the '{}'", key, bucketName);
  }

  /**
   * This method provides a list of all the object keys stored in the s3 bucket.
   *
   * @return the list of keys of every file in the bucket.
   * @throws NoSuchBucketException The specified bucket does not exist.
   * @throws SdkException Base class for all exceptions that can be thrown by the SDK (both service
   *     and client). Can be used for catch all scenarios.
   * @throws SdkClientException If any client side error occurs such as an IO related failure,
   *     failure to get credentials, etc.
   * @throws S3Exception Base class for all service exceptions. Unknown exceptions will be thrown as
   *     an instance of this type.
   */
  public List<String> listings() {
    return s3Client
        .listObjects(ListObjectsRequest.builder().bucket(bucketName).build())
        .contents()
        .stream()
        .map(S3Object::key)
        .toList();
  }

  /**
   * Deletes the specified object in the configured bucket. Successful when the object is deleted or
   * the object does not exist.
   *
   * @param key the key of the file to be deleted in s3 bucket
   * @throws SdkException Base class for all exceptions that can be thrown by the SDK (both service
   *     and client). Can be used for catch all scenarios.
   * @throws SdkClientException If any client side error occurs such as an IO related failure,
   *     failure to get credentials, etc.
   * @throws S3Exception Base class for all service exceptions. Unknown exceptions will be thrown as
   *     an instance of this type.
   */
  public void deleteFile(String key) {
    LOG.info("Deleting file with key='{}' ...", key);
    s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
    LOG.info("Done! File with key='{}' deleted successfully!", key);
  }

  /**
   * This method verifies if an object exists in the s3 bucket or not
   *
   * @param key the key of the object
   * @return true, if object exist.
   * @throws NoSuchBucketException The specified bucket does not exist.
   * @throws SdkException Base class for all exceptions that can be thrown by the SDK (both service
   *     and client). Can be used for catch all scenarios.
   * @throws SdkClientException If any client side error occurs such as an IO related failure,
   *     failure to get credentials, etc.
   * @throws S3Exception Base class for all service exceptions. Unknown exceptions will be thrown as
   *     an instance of this type.
   */
  public boolean doesObjectExist(String key) {
    return listings().contains(key);
  }
}
