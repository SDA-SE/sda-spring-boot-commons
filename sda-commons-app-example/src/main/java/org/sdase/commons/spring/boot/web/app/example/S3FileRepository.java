/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import org.sdase.commons.spring.boot.s3.config.S3BucketRepository;
import org.springframework.stereotype.Component;

@Component
public class S3FileRepository {

  private final S3BucketRepository s3Repository;

  public S3FileRepository(S3BucketRepository s3Repository) {
    this.s3Repository = s3Repository;
  }

  public void uploadTextFile(String key, String text) {
    try {
      s3Repository.save(key, text);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public String downloadText(String key) {
    return new String(s3Repository.findByName(key), StandardCharsets.UTF_8);
  }
}
