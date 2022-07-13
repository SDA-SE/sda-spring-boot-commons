/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.s3.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class S3BucketRepositoryTest {
  @Mock AmazonS3 amazonS3;

  @InjectMocks S3BucketRepository subject;

  @BeforeEach
  void setup() throws IllegalAccessException {
    FieldUtils.writeField(subject, "bucketName", "test-bucket", true);
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
  void shouldDeleteObject() {
    doNothing().when(amazonS3).deleteObject("bucketName", "keyName");
    amazonS3.deleteObject("bucketName", "keyName");
    verify(amazonS3, times(1)).deleteObject("bucketName", "keyName");
  }
}
