/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

// ATTENTION: The source of this class is included in the public documentation.

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.INPUT_STREAM;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.testing.s3.LocalS3Configuration;
import org.sdase.commons.spring.boot.web.testing.s3.S3Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.S3Object;

@S3Test
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {"management.server.port=0"})
class S3FileRepositoryIntegrationTest {

  @Autowired S3FileRepository s3FileRepository;

  @Test
  void shouldUploadTextFile(S3Client s3Client, LocalS3Configuration config) {
    var givenKey = "test-file.txt";
    var givenText = "Hello World!";

    s3FileRepository.uploadTextFile(givenKey, givenText);

    var actualContent = s3Client.listObjects(l -> l.bucket(config.bucketName()));
    assertThat(actualContent.contents())
        .hasSize(1)
        .first()
        .extracting(S3Object::key)
        .isEqualTo("test-file.txt")
        .extracting(k -> s3Client.getObject(o -> o.bucket(config.bucketName()).key(k)))
        .asInstanceOf(INPUT_STREAM)
        .hasContent("Hello World!");
  }

  @Test
  void shouldDownloadTextFile(S3Client s3Client, LocalS3Configuration config) {
    var givenKey = "existing-test-file.txt";
    var givenText = "Hello World!";
    s3Client.putObject(
        o -> o.bucket(config.bucketName()).key(givenKey),
        RequestBody.fromBytes(givenText.getBytes(StandardCharsets.UTF_8)));

    String actualText = s3FileRepository.downloadText(givenKey);

    assertThat(actualText).isEqualTo("Hello World!");
  }
}
