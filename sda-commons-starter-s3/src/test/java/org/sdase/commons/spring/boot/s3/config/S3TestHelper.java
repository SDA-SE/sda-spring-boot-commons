/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteBucketRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

public class S3TestHelper {

  private static final Logger LOG = LoggerFactory.getLogger(S3TestHelper.class);

  private final String testBucket;
  private final S3Client s3Client;

  public S3TestHelper(String testBucket, S3Client s3Client) {
    this.testBucket = testBucket;
    this.s3Client = s3Client;
  }

  public void deleteTestBucket() {
    try {
      deleteAllObjects();
      s3Client.deleteBucket(DeleteBucketRequest.builder().bucket(testBucket).build());
    } catch (NoSuchBucketException e) {
      // ignore
    }
  }

  void deleteAllObjects() {
    var response = s3Client.listObjects(ListObjectsRequest.builder().bucket(testBucket).build());
    response.contents().stream()
        .map(
            s3Object ->
                DeleteObjectRequest.builder().bucket(testBucket).key(s3Object.key()).build())
        .forEach(s3Client::deleteObject);
    if (!response.sdkHttpResponse().isSuccessful()) {
      LOG.info("Could not delete objects in bucket: {}", testBucket);
    }
  }
}
