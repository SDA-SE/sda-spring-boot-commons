package org.sdase.commons.spring.boot.s3.config;


import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Service
public interface S3BucketOperations {

   byte[] findByName(String fileName);


   void save(MultipartFile multipartFile);
   List<String> listings();

   void deleteFile(final String keyName);
}
