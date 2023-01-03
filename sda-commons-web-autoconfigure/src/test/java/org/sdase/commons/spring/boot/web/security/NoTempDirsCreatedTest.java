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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * This test verifies that Spring Boot will not create temporary directories on startup. As writable
 * temp directories are often part of security breaches, it is recommended to have read only file
 * systems in Docker environments. The Spring Boot applications built with sda-spring-boot-commons
 * will not require write access by default. This means, no writable temp dir must be mounted in the
 * container.
 *
 * <p>If an application provides multipart file upload support, a writable <code>/tmp</code> dir is
 * needed.
 *
 * <p>There should be no issue with multipart uploads when directories are not created at startup.
 * Whenever a file is uploaded, Tomcat will <a
 * href="https://github.com/apache/tomcat/commit/267b8d8852db44dbad249453099ef6e9c26a4e9f">create
 * the required directories</a>. This feature is <a
 * href="https://github.com/spring-projects/spring-boot/blob/v2.1.4.RELEASE/spring-boot-project/spring-boot/src/main/java/org/springframework/boot/web/embedded/tomcat/TomcatServletWebServerFactory.java#L210">enabled
 * in Spring Boot since 2.1.4</a>.
 */
@SpringBootTest(
    classes = NoTempDirsCreatedTest.TestApp.class,
    webEnvironment = RANDOM_PORT,
    properties = {
      "management.server.port=8082"
    }) // FIXME using defined port right now because there where situations where multiple
// management servers where started with random port. In that situations a defined port
// failed to start the second management server.
class NoTempDirsCreatedTest {

  static final Logger LOG = LoggerFactory.getLogger(NoTempDirsCreatedTest.class);
  static final String TMP_DIR_PROP = "java.io.tmpdir";

  static File tempDir = new File(System.getProperty(TMP_DIR_PROP));

  @Autowired TestApp testApp;

  @Test
  void shouldNotCreateTempDir() {
    assertThat(getFilesInTempDir()).containsExactlyElementsOf(TestApp.beforeTempDirs);
  }

  @Test
  void shouldHaveSystemDependentBaseDirConfigured() {
    assertThat(testApp.getBaseDir()).isEqualTo(System.getProperty(TMP_DIR_PROP) + "/tomcat");
  }

  private static Set<File> getFilesInTempDir() {
    LOG.info("Checking files in temp dir {}", tempDir);
    //noinspection ConstantConditions
    return new HashSet<>(Arrays.asList(tempDir.listFiles()));
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
