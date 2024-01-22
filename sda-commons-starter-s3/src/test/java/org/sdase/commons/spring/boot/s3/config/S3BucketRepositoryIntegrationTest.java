/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.robothy.s3.jupiter.LocalS3;
import com.robothy.s3.jupiter.LocalS3Endpoint;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.CreateBucketRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Object;

@LocalS3
class S3BucketRepositoryIntegrationTest {

  static final String TEST_BUCKET = "test-bucket";

  private LocalS3Endpoint endpoint;
  private S3Client s3Client;

  @BeforeEach
  void beforeEach(S3Client s3Client, LocalS3Endpoint endpoint) {
    this.s3Client = s3Client;
    this.endpoint = endpoint;
    new S3TestHelper(TEST_BUCKET, s3Client).deleteTestBucket();
  }

  @Test
  void shouldListObjects() {

    createObjects(s3Client, Map.of("file-1", "content-1", "file-2", "content-2"));
    S3BucketRepository s3BucketRepository = createS3BucketRepository(endpoint);

    var actual = s3BucketRepository.listings();
    assertThat(actual).containsExactlyInAnyOrder("file-1", "file-2");
  }

  @Test
  void shouldFindOutIfObjectExists() {

    createObjects(s3Client, Map.of("file-1", "content-1", "file-2", "content-2"));
    S3BucketRepository s3BucketRepository = createS3BucketRepository(endpoint);

    var actual =
        List.of(
            s3BucketRepository.doesObjectExist("file-1"),
            s3BucketRepository.doesObjectExist("file-2"),
            s3BucketRepository.doesObjectExist("file-3"));

    assertThat(actual).containsExactly(true, true, false);
  }

  @Test
  void shouldDeleteFile() {

    createObjects(s3Client, Map.of("file-1", "content-1", "file-2", "content-2"));
    S3BucketRepository s3BucketRepository = createS3BucketRepository(endpoint);

    s3BucketRepository.deleteFile("file-1");

    ListObjectsResponse response =
        s3Client.listObjects(ListObjectsRequest.builder().bucket(TEST_BUCKET).build());
    assertThat(response.contents()).hasSize(1).extracting(S3Object::key).containsExactly("file-2");
  }

  @Test
  void shouldGetObject() {

    createObjects(s3Client, Map.of("file-1", "content-1"));
    S3BucketRepository s3BucketRepository = createS3BucketRepository(endpoint);

    var actual = s3BucketRepository.findByName("file-1");
    assertThat(actual).asString().isEqualTo("content-1");
  }

  @Test
  void shouldGetUtf8Object() {

    createObjects(s3Client, Map.of("file-1", "content-1 with ä"));
    S3BucketRepository s3BucketRepository = createS3BucketRepository(endpoint);

    var actual = s3BucketRepository.findByName("file-1");
    assertThat(actual).contains("content-1 with ä".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void shouldNotSaveNullContent() {

    createObjects(s3Client, Map.of());
    S3BucketRepository s3BucketRepository = createS3BucketRepository(endpoint);

    assertThatExceptionOfType(IOException.class)
        .isThrownBy(() -> s3BucketRepository.save("test-object", null));
  }

  @Test
  void shouldSaveContent() throws Exception {

    createObjects(s3Client, Map.of());
    // precondition
    ListObjectsResponse response =
        s3Client.listObjects(ListObjectsRequest.builder().bucket(TEST_BUCKET).build());
    assertThat(response.contents()).isEmpty();
    S3BucketRepository s3BucketRepository = createS3BucketRepository(endpoint);

    s3BucketRepository.save("test-object", "test-content");

    try (ResponseInputStream<GetObjectResponse> responseInputStream =
        s3Client.getObject(
            GetObjectRequest.builder().bucket(TEST_BUCKET).key("test-object").build())) {
      assertThat(new String(responseInputStream.readAllBytes(), StandardCharsets.UTF_8))
          .isEqualTo("test-content");
    }
  }

  @Test
  void shouldSaveUtf8Content() throws IOException {

    createObjects(s3Client, Map.of());
    // precondition
    ListObjectsResponse response =
        s3Client.listObjects(ListObjectsRequest.builder().bucket(TEST_BUCKET).build());
    assertThat(response.contents()).isEmpty();
    S3BucketRepository s3BucketRepository = createS3BucketRepository(endpoint);

    s3BucketRepository.save("test-utf8-object", "test-content with ä");

    try (ResponseInputStream<GetObjectResponse> responseInputStream =
        s3Client.getObject(
            GetObjectRequest.builder().bucket(TEST_BUCKET).key("test-utf8-object").build())) {
      assertThat(responseInputStream.readAllBytes())
          .isEqualTo("test-content with ä".getBytes(StandardCharsets.UTF_8));
    }
  }

  @Test
  void shouldSaveUtf8File() throws IOException, URISyntaxException {

    File testFile =
        new File(Objects.requireNonNull(getClass().getResource("/test-data/some-text.md")).toURI());
    createObjects(s3Client, Map.of());
    // precondition
    ListObjectsResponse response =
        s3Client.listObjects(ListObjectsRequest.builder().bucket(TEST_BUCKET).build());
    assertThat(response.contents()).isEmpty();
    S3BucketRepository s3BucketRepository = createS3BucketRepository(endpoint);

    s3BucketRepository.saveFile("test-utf8-file", testFile);

    try (ResponseInputStream<GetObjectResponse> responseInputStream =
        s3Client.getObject(
            GetObjectRequest.builder().bucket(TEST_BUCKET).key("test-utf8-file").build())) {
      assertThat(responseInputStream.readAllBytes())
          .asString(StandardCharsets.UTF_8)
          .isEqualTo(Files.readString(testFile.toPath(), StandardCharsets.UTF_8));
    }
  }

  @Test
  void shouldSaveBinaryFile() throws IOException, URISyntaxException {

    File testFile =
        new File(
            Objects.requireNonNull(getClass().getResource("/test-data/some-text.md.zip")).toURI());
    createObjects(s3Client, Map.of());
    // precondition
    ListObjectsResponse response =
        s3Client.listObjects(ListObjectsRequest.builder().bucket(TEST_BUCKET).build());
    assertThat(response.contents()).isEmpty();
    S3BucketRepository s3BucketRepository = createS3BucketRepository(endpoint);

    s3BucketRepository.saveFile("test-binary-file", testFile);

    try (ResponseInputStream<GetObjectResponse> responseInputStream =
        s3Client.getObject(
            GetObjectRequest.builder().bucket(TEST_BUCKET).key("test-binary-file").build())) {
      assertThat(responseInputStream.readAllBytes())
          .isEqualTo(Files.readAllBytes(testFile.toPath()));
    }
  }

  private S3BucketRepository createS3BucketRepository(LocalS3Endpoint endpoint) {
    S3Configuration s3Configuration =
        new S3Configuration("", "", endpoint.endpoint(), endpoint.region(), TEST_BUCKET);
    return s3Configuration.s3BucketRepository(s3Configuration.getAmazonS3Client());
  }

  private void createObjects(S3Client testClient, Map<String, String> objectsByKey) {
    try {
      testClient.headBucket(HeadBucketRequest.builder().bucket(TEST_BUCKET).build());
    } catch (NoSuchBucketException e) {
      testClient.createBucket(CreateBucketRequest.builder().bucket(TEST_BUCKET).build());
    }

    objectsByKey.forEach(
        (key, content) ->
            testClient.putObject(
                PutObjectRequest.builder().bucket(TEST_BUCKET).key(key).build(),
                RequestBody.fromString(content)));
  }
}
