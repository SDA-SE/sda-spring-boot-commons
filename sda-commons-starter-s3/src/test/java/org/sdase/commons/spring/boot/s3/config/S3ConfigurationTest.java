/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.s3.S3TestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = S3TestApp.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class S3ConfigurationTest {

  @Autowired private S3Configuration s3Configuration;

  @Autowired private S3BucketRepository s3BucketRepository;

  @Test
  public void test_configurationAutowiredCorrectly() {
    assertNotNull(s3Configuration, "Configuration expected to be not null!");

    assertEquals("sdase", s3Configuration.getAccessKeyId(), "AccessKeyId does not match");
    assertEquals("test1234", s3Configuration.getSecretKey(), "SecretKey does not match");
    assertEquals("eu-west3", s3Configuration.getRegion(), "Region does not match");
    assertEquals(
        "http://localhost:37012", s3Configuration.getEndpoint(), "Endpoint does not match");
    assertEquals("test-bucket", s3Configuration.getBucketName(), "BucketName does not match");
  }

  @Test
  public void test_s3BucketRepositoryAutowiredCorrectly() {
    assertNotNull(s3BucketRepository, "BucketRepository expected to be not null!");
  }
}
