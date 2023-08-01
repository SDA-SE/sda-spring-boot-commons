/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class S3BucketRepositoryTest {
  // TODO API and packages changed
  //  public static final String BUCKET_NAME = "test-bucket";
  //  @Mock private AmazonS3 amazonS3;
  //
  //  S3BucketRepository subject;
  //
  //  @BeforeEach
  //  void setup() {
  //    subject = new S3BucketRepository(amazonS3, BUCKET_NAME);
  //  }
  //
  //  @Test
  //  void shouldFindByName() throws IOException {
  //    String expected = "Test Object Content";
  //    InputStream inputStream = new ByteArrayInputStream(expected.getBytes());
  //    S3ObjectInputStream s3Input = new S3ObjectInputStream(inputStream, null);
  //    S3Object s3Object = mock(S3Object.class);
  //    when(amazonS3.getObject(anyString(), anyString())).thenReturn(s3Object);
  //    when(s3Object.getObjectContent()).thenReturn(s3Input);
  //
  //    byte[] response = subject.findByName("testFileName");
  //    assertThat(response).isNotEmpty().isEqualTo(expected.getBytes());
  //
  //    verify(s3Object).close();
  //  }
  //
  //  @Test
  //  void shouldDisplayListings() {
  //    ObjectListing objectListing = new ObjectListing();
  //    S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
  //    s3ObjectSummary.setKey("test-Key");
  //    objectListing.getObjectSummaries().add(s3ObjectSummary);
  //
  //    when(amazonS3.listObjects(anyString())).thenReturn(objectListing);
  //
  //    List<String> result = subject.listings();
  //    assertThat(result).hasSize(1).contains("test-Key");
  //  }
  //
  //  @Test
  //  void shouldThrowFileNotFoundException() {
  //    File file = mock(File.class);
  //
  //    assertThatExceptionOfType(FileNotFoundException.class)
  //        .isThrownBy(() -> subject.saveFile("key", file))
  //        .withMessageContaining(String.format("No file exists in the provided path %s ", file));
  //  }
  //
  //  @Test
  //  void shouldThrowIOExceptionIfContentIsNull() {
  //    String key = "some-key";
  //
  //    assertThatExceptionOfType(IOException.class)
  //        .isThrownBy(() -> subject.save(key, null))
  //        .withMessageContaining(
  //            String.format("Content to be saved by key %s must not be null!", key));
  //  }
  //
  //  @Test
  //  void shouldSaveContentAsString() throws IOException {
  //    String key = "some-key";
  //    String content = "this is the content to be saved";
  //
  //    subject.save(key, content);
  //
  //    verify(amazonS3, times(1)).putObject(BUCKET_NAME, key, content);
  //  }
  //
  //  @Test
  //  void shouldDeleteObject() {
  //    doNothing().when(amazonS3).deleteObject(anyString(), anyString());
  //    subject.deleteFile("test-Key");
  //    verify(amazonS3, times(1)).deleteObject(BUCKET_NAME, "test-Key");
  //  }
  //
  //  @Test
  //  void objectShouldExistInS3Bucket() {
  //    S3BucketRepository repository = mock(S3BucketRepository.class);
  //    when(repository.doesObjectExist(anyString())).thenReturn(true);
  //    assertThat(repository.doesObjectExist("key")).isTrue();
  //  }
  //
  //  @Test
  //  void ObjectShouldNotExistInS3Bucket() {
  //    S3BucketRepository repository = mock(S3BucketRepository.class);
  //    when(repository.doesObjectExist(anyString())).thenReturn(false);
  //    assertThat(repository.doesObjectExist("some key")).isFalse();
  //  }
}
