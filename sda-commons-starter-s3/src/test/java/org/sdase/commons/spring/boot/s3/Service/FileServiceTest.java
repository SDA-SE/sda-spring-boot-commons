package org.sdase.commons.spring.boot.s3.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

  @InjectMocks
  FileService subject;

  @Mock
  AmazonS3 amazonS3;

  @BeforeEach
  void setup() throws IllegalAccessException {
    FieldUtils.writeField(subject, "bucketName", "test-bucket", true);
  }

  @Test
  void shouldFindByName() {
    String expected = "Test Object Content";
    InputStream inputStream = IOUtils.toInputStream(expected);
    S3ObjectInputStream s3Input = new S3ObjectInputStream(inputStream, null);
    S3Object s3Object = mock(S3Object.class);
    when(amazonS3.getObject(anyString(), anyString())).thenReturn(s3Object);
    when(s3Object.getObjectContent()).thenReturn(s3Input);

    byte[] response = subject.findByName("testFileName");
    assertNotEquals(0, response.length);
    assertArrayEquals(expected.getBytes(), response);
  }
}
