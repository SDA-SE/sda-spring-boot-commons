package org.sdase.commons.spring.boot.s3.Controller;

import org.sdase.commons.spring.boot.s3.Service.FileService;
import org.springframework.core.io.ByteArrayResource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/files")
public class FileController {

  private static final String FILE_NAME = "fileName";

  private FileService fileService;

  public FileController(final FileService fileService) {
    this.fileService = fileService;
  }

  @GetMapping("/listings")
  public ResponseEntity<Object> listings() {
    return ResponseEntity.ok(fileService.listings());
  }

  @GetMapping
  public ResponseEntity<ByteArrayResource> findByName(
      @RequestBody(required = false) Map<String, String> params) {
    final byte[] data = fileService.findByName(params.get(FILE_NAME));
    final ByteArrayResource resource = new ByteArrayResource(data);
    return ResponseEntity.ok()
        .contentLength(data.length)
        .header("Content-disposition", "attachment; filename=\"" + params.get(FILE_NAME) + "\"")
        .body(resource);
  }

  @PostMapping
  public ResponseEntity<Object> saveFile(@RequestParam("file") MultipartFile file) {
    fileService.save(file);
    return ResponseEntity.ok().build();
  }
}

