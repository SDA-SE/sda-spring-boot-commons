/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This test verifies that Spring Boot will use the configured java.io.tmpdir property to use as the
 * tmp directory
 */
@SpringBootTest(classes = SystemDependentBaseDirTest.TestApp.class, webEnvironment = RANDOM_PORT)
class SystemDependentBaseDirTest {

  static final Logger LOG = LoggerFactory.getLogger(SystemDependentBaseDirTest.class);
  static final String TMP_DIR_PROP = "java.io.tmpdir";

  static Path staticDir = createStaticDirectory();

  static File tempDir = new File(System.getProperty(TMP_DIR_PROP));

  @Autowired TestApp testApp;

  @AfterAll
  static void afterAll() {
    deleteStaticDirectory(staticDir);
  }

  @Test
  void shouldNotCreateTempDir() {
    assertThat(getFilesInTempDir()).containsExactlyElementsOf(TestApp.beforeTempDirs);
  }

  @Test
  void shouldHaveSystemDependentBaseDirConfigured() {
    assertThat(testApp.getBaseDir()).endsWith("tomcat");
  }

  private static Set<File> getFilesInTempDir() {
    LOG.info("Checking files in temp dir {}", tempDir);
    //noinspection ConstantConditions
    return new HashSet<>(Arrays.asList(tempDir.listFiles()));
  }

  private static Path createStaticDirectory() {
    try {
      Path root = Paths.get(".").normalize().toAbsolutePath();
      Path filePath = Paths.get(root.toString(), "static");

      deleteStaticDirectory(filePath);

      return Files.createDirectory(filePath);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void deleteStaticDirectory(Path path) {
    try {
      try (var dirFiles = Files.walk(path).sorted(Comparator.reverseOrder()).map(Path::toFile)) {
        dirFiles.forEach(File::delete);
      }
      Files.deleteIfExists(path);
    } catch (NoSuchFileException nsfe) {
      LOG.info("Static directory not found, skipping deletion : " + nsfe.getMessage());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @SpringBootApplication
  public static class TestApp {
    // evaluated after app start when declared in test class
    static final Set<File> beforeTempDirs = getFilesInTempDir();

    @Value("${server.tomcat.basedir}")
    private String baseDir;

    public String getBaseDir() {
      return baseDir;
    }
  }
}
