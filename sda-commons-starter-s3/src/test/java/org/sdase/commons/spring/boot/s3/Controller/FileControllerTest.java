package org.sdase.commons.spring.boot.s3.Controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sdase.commons.spring.boot.s3.Service.FileService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

  private static final String FILE_NAME = "fileName";

  private static final String CONTENT_DISPOSITION_HEADER = "Content-disposition";

  @InjectMocks
  FileController subject;

  @Mock
  FileService fileService;

  @Test
  void shouldReturnListings() {
    ResponseEntity<Object> response = subject.listings();
    assertEquals(200, response.getStatusCodeValue());
  }

  @Test
  void shouldFindByName() {
    Map<String, String> params = new HashMap<>();
    params.put(FILE_NAME, "testFileName");
    when(fileService.findByName("testFileName")).thenReturn("TestFile".getBytes());

    ResponseEntity<ByteArrayResource> response = subject.findByName(params);
    List<String> headerList = response.getHeaders().get(CONTENT_DISPOSITION_HEADER);
    assertEquals("attachment; filename=\"testFileName\"", headerList.get(0));
    assertEquals(200, response.getStatusCodeValue());
  }

  @Test
  void shouldTestFile() {
    MockMultipartFile file = new MockMultipartFile("filename.txt", "testfileContent".getBytes());
    ResponseEntity<Object> response = subject.saveFile(file);
    assertEquals(200, response.getStatusCodeValue());
  }
}
