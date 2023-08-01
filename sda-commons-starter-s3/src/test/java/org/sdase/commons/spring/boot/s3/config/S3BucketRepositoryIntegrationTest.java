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

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.AnonymousAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import io.findify.s3mock.S3Mock;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class S3BucketRepositoryIntegrationTest {

  static final String TEST_REGION = "eu-central-1";
  static final String TEST_BUCKET = "test-bucket";

  static S3Mock s3Mock;
  static String endpoint;
  static AmazonS3 testClient;

  @BeforeAll
  static void initS3Mock() {
    int port = getFreePort();
    s3Mock = new S3Mock.Builder().withInMemoryBackend().withPort(port).build();
    s3Mock.start();
    endpoint = "http://localhost:" + port;

    testClient =
        AmazonS3ClientBuilder.standard()
            .withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration(endpoint, TEST_REGION))
            .withPathStyleAccessEnabled(true)
            .withCredentials(new AWSStaticCredentialsProvider(new AnonymousAWSCredentials()))
            .build();
  }

  @AfterAll
  static void afterAll() {
    s3Mock.stop();
  }

  @AfterEach
  void removeBucket() {
    testClient.deleteBucket(TEST_BUCKET);
  }

  @Test
  void shouldListObjects() {
    createObjects(Map.of("file-1", "content-1", "file-2", "content-2"));
    S3BucketRepository s3BucketRepository = createS3BucketRepository();

    var actual = s3BucketRepository.listings();
    assertThat(actual).containsExactlyInAnyOrder("file-1", "file-2");
  }

  @Test
  void shouldFindOutIfObjectExists() {
    createObjects(Map.of("file-1", "content-1", "file-2", "content-2"));
    S3BucketRepository s3BucketRepository = createS3BucketRepository();

    var actual =
        List.of(
            s3BucketRepository.doesObjectExist("file-1"),
            s3BucketRepository.doesObjectExist("file-2"),
            s3BucketRepository.doesObjectExist("file-3"));

    assertThat(actual).containsExactly(true, true, false);
  }

  @Test
  void shouldDeleteFile() {
    createObjects(Map.of("file-1", "content-1", "file-2", "content-2"));
    S3BucketRepository s3BucketRepository = createS3BucketRepository();

    s3BucketRepository.deleteFile("file-1");

    assertThat(testClient.listObjects(TEST_BUCKET).getObjectSummaries())
        .hasSize(1)
        .extracting(S3ObjectSummary::getKey)
        .containsExactly("file-2");
  }

  @Test
  void shouldGetObject() {
    createObjects(Map.of("file-1", "content-1"));
    S3BucketRepository s3BucketRepository = createS3BucketRepository();

    var actual = s3BucketRepository.findByName("file-1");
    assertThat(actual).asString().isEqualTo("content-1");
  }

  @Test
  void shouldGetUtf8Object() {
    createObjects(Map.of("file-1", "content-1 with ä"));
    S3BucketRepository s3BucketRepository = createS3BucketRepository();

    var actual = s3BucketRepository.findByName("file-1");
    assertThat(actual).contains("content-1 with ä".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void shouldNotSaveNullContent() {
    createObjects(Map.of());
    S3BucketRepository s3BucketRepository = createS3BucketRepository();

    assertThatExceptionOfType(IOException.class)
        .isThrownBy(() -> s3BucketRepository.save("test-object", null));
  }

  @Test
  void shouldSaveContent() throws IOException {
    createObjects(Map.of());
    // precondition
    assertThat(testClient.listObjects(TEST_BUCKET).getObjectSummaries()).isEmpty();
    S3BucketRepository s3BucketRepository = createS3BucketRepository();

    s3BucketRepository.save("test-object", "test-content");

    assertThat(testClient.getObject(TEST_BUCKET, "test-object").getObjectContent())
        .hasContent("test-content");
  }

  @Test
  void shouldSaveUtf8Content() throws IOException {
    createObjects(Map.of());
    // precondition
    assertThat(testClient.listObjects(TEST_BUCKET).getObjectSummaries()).isEmpty();
    S3BucketRepository s3BucketRepository = createS3BucketRepository();

    s3BucketRepository.save("test-utf8-object", "test-content with ä");

    assertThat(testClient.getObject(TEST_BUCKET, "test-utf8-object").getObjectContent())
        .hasBinaryContent("test-content with ä".getBytes(StandardCharsets.UTF_8));
  }

  @Test
  void shouldSaveUtf8File() throws IOException, URISyntaxException {
    File testFile =
        new File(Objects.requireNonNull(getClass().getResource("/test-data/some-text.md")).toURI());
    createObjects(Map.of());
    // precondition
    assertThat(testClient.listObjects(TEST_BUCKET).getObjectSummaries()).isEmpty();
    S3BucketRepository s3BucketRepository = createS3BucketRepository();

    s3BucketRepository.saveFile("test-utf8-file", testFile);

    assertThat(testClient.getObject(TEST_BUCKET, "test-utf8-file").getObjectContent())
        .hasSameContentAs(new FileInputStream(testFile));
  }

  @Test
  void shouldSaveBinaryFile() throws IOException, URISyntaxException {
    File testFile =
        new File(
            Objects.requireNonNull(getClass().getResource("/test-data/some-text.md.zip")).toURI());
    createObjects(Map.of());
    // precondition
    assertThat(testClient.listObjects(TEST_BUCKET).getObjectSummaries()).isEmpty();
    S3BucketRepository s3BucketRepository = createS3BucketRepository();

    s3BucketRepository.saveFile("test-binary-file", testFile);

    assertThat(testClient.getObject(TEST_BUCKET, "test-binary-file").getObjectContent())
        .hasSameContentAs(new FileInputStream(testFile));
  }

  private static S3BucketRepository createS3BucketRepository() {
    S3Configuration s3Configuration =
        new S3Configuration("", "", endpoint, TEST_REGION, TEST_BUCKET);
    return s3Configuration.s3BucketRepository(s3Configuration.getAmazonS3Client());
  }

  private void createObjects(Map<String, String> objectsByKey) {
    if (!testClient.doesBucketExistV2(TEST_BUCKET)) {
      testClient.createBucket(TEST_BUCKET);
    }
    objectsByKey.forEach((key, content) -> testClient.putObject(TEST_BUCKET, key, content));
  }

  private static int getFreePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      socket.setReuseAddress(true);
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not find a free port.", e);
    }
  }
}
