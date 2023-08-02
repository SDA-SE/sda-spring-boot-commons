/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.s3.S3TestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = S3TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
class S3ConfigurationTest {

  @Autowired private S3Configuration s3Configuration;

  @Autowired private S3BucketRepository s3BucketRepository;

  @Test
  void test_configurationAutowiredCorrectly() {
    assertThat(s3Configuration)
        .isNotNull()
        .extracting("accessKeyId", "secretKey", "region", "endpoint", "bucketName")
        .contains("sdase", "test1234", "eu-west3", "http://localhost:37012", "test-bucket");
  }

  @Test
  void test_s3BucketRepositoryAutowiredCorrectly() {
    assertThat(s3BucketRepository).isNotNull();
  }
}
