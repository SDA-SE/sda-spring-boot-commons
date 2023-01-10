/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.s3.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import java.io.*;
import java.util.List;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class S3BucketRepositoryTest {

  public static final String BUCKET_NAME = "test-bucket";
  @Mock private AmazonS3 amazonS3;

  @InjectMocks S3BucketRepository subject;

  @BeforeEach
  void setup() throws IllegalAccessException {
    FieldUtils.writeField(subject, "bucketName", BUCKET_NAME, true);
  }

  @Test
  void shouldFindByName() throws IOException {
    String expected = "Test Object Content";
    InputStream inputStream = new ByteArrayInputStream(expected.getBytes());
    S3ObjectInputStream s3Input = new S3ObjectInputStream(inputStream, null);
    S3Object s3Object = mock(S3Object.class);
    when(amazonS3.getObject(anyString(), anyString())).thenReturn(s3Object);
    when(s3Object.getObjectContent()).thenReturn(s3Input);

    byte[] response = subject.findByName("testFileName");
    assertNotEquals(0, response.length);
    assertArrayEquals(expected.getBytes(), response);
  }

  @Test
  void shouldDisplayListings() {
    ObjectListing objectListing = new ObjectListing();
    S3ObjectSummary s3ObjectSummary = new S3ObjectSummary();
    s3ObjectSummary.setKey("test-Key");
    objectListing.getObjectSummaries().add(s3ObjectSummary);

    when(amazonS3.listObjects(anyString())).thenReturn(objectListing);

    List<String> result = subject.listings();
    assertEquals(1, result.size());
    assertThat(result).contains("test-Key");
  }

  @Test
  void shouldThrowFileNotFoundException() {
    File file = mock(File.class);

    FileNotFoundException exception =
        assertThrows(FileNotFoundException.class, () -> subject.saveFile("key", file));

    String expectedMessage = String.format("No file exists in the provided path %s ", file);
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void shouldThrowIOExceptionIfContentIsNull() {
    String key = "some-key";
    IOException exception = assertThrows(IOException.class, () -> subject.save(key, null));

    String expectedMessage = String.format("Content to be saved by key %s must not be null!", key);
    String actualMessage = exception.getMessage();

    assertTrue(actualMessage.contains(expectedMessage));
  }

  @Test
  void shouldSaveContentAsString() throws IOException {
    String key = "some-key";
    String content = "this is the content to be saved";

    subject.save(key, content);

    verify(amazonS3, times(1)).putObject(BUCKET_NAME, key, content);
  }

  @Test
  void shouldDeleteObject() {
    doNothing().when(amazonS3).deleteObject(anyString(), anyString());
    amazonS3.deleteObject(BUCKET_NAME, "test-Key");
    verify(amazonS3, times(1)).deleteObject(BUCKET_NAME, "test-Key");
  }

  @Test
  void objectShouldExistInS3Bucket() {
    S3BucketRepository repository = mock(S3BucketRepository.class);
    when(repository.doesObjectExist(anyString())).thenReturn(true);
    assertTrue(repository.doesObjectExist("key"));
  }

  @Test
  void ObjectShouldNotExistInS3Bucket() {
    S3BucketRepository repository = mock(S3BucketRepository.class);
    when(repository.doesObjectExist(anyString())).thenReturn(false);
    assertFalse(repository.doesObjectExist("some key"));
  }
}
