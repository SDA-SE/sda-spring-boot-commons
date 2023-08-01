/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

import io.awspring.cloud.s3.S3Exception;
import io.awspring.cloud.s3.S3Operations;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.utils.IoUtils;

public class S3BucketRepository {

  private static final Logger LOG = LoggerFactory.getLogger(S3BucketRepository.class);
  private final String bucketName;
  private final S3Operations s3Operations;

  public S3BucketRepository(S3Operations s3Operations, String bucketName) {
    this.s3Operations = s3Operations;
    this.bucketName = bucketName;
  }

  /**
   * Gets the object stored in Amazon S3 under the configured bucket and passed key.
   *
   * @param key The key under which the desired object is stored.
   * @return the content of the object as byte array in the S3 bucket with the given key.
   * @throws FileNotFoundException – if the underlying resource does not exist
   * @throws IOException – if the content stream could not be opened
   */
  public byte[] findByName(String key) throws IOException {
    var resource = s3Operations.download(bucketName, key);
    try (var is = resource.getInputStream()) {
      return IoUtils.toByteArray(is);
    }
  }

  /**
   * Uploads file to s3 bucket to the configured bucket name. By passing the file and the key in
   * which the file is to be uploaded to.
   *
   * @param key The key under which to store the specified file
   * @param file The file containing the data to be uploaded to Amazon S3.
   * @throws FileNotFoundException if the file does not exist, is a directory rather than a regular
   *     file, or for some other reason cannot be opened for reading.
   * @throws SecurityException if a security manager exists and its checkRead method denies read
   *     access to the file.
   * @throws S3Exception if an error occurs on upload
   * @throws IOException if the given file can't be closed after reading
   */
  public void saveFile(String key, File file) throws S3Exception, IOException, SecurityException {
    LOG.info("Uploading file with name {}...", file.getName());
    try (var is = new FileInputStream(file)) {
      s3Operations.upload(bucketName, key, is);
      LOG.info(
          "Done! File with name '{}' has been uploaded to the '{}'", file.getName(), bucketName);
    }
  }

  /**
   * Uploads data as string to s3 bucket to the configured bucket name. By passing the content and
   * the key in which the file is to be uploaded to.
   *
   * @param key The key under which to store the specified content
   * @param content The data to be uploaded to Amazon S3. Will be saved as UTF-8.
   * @throws IOException If the content is null
   * @throws S3Exception if an error occurs on upload
   */
  public void save(String key, String content) throws IOException {
    if (content == null) {
      throw new IOException(String.format("Content to be saved by key %s must not be null!", key));
    }
    LOG.info("Uploading data with key {}...", key);
    try (var is = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8))) {
      s3Operations.upload(bucketName, key, is);
      LOG.info("Done! Data with key '{}' has been uploaded to the '{}'", key, bucketName);
    }
  }

  // TODO needs to be deprecated in current release and then removed in v3
  public List<String> listings() {
    throw new UnsupportedOperationException("This method will not be provided any more.");
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
    s3Operations.deleteObject(bucketName, key);
    LOG.info("Done! File with key='{}' deleted successfully!", key);
  }

  /**
   * This method verifies if an object exists in the s3 bucket or not
   *
   * @param key the key of the object
   * @return true, if object exist.
   */
  public boolean doesObjectExist(String key) {
    return s3Operations.download(bucketName, key).exists();
  }
}
