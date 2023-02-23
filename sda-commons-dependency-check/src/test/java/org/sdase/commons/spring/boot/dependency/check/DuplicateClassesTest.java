/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.dependency.check;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.Resource;
import io.github.classgraph.ResourceList;
import io.github.classgraph.ScanResult;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class DuplicateClassesTest {

  private static final List<Pattern> ignorePatterns =
      List.of(
          // Spring JCL duplicates classes of commons logging
          Pattern.compile("org/apache/commons/logging/.*"),
          // AspectJ duplicates its own classes in various modules
          Pattern.compile("org/aspectj/.*"),
          // Tomcat embed duplicates the servlet classes
          Pattern.compile("javax/servlet/.*"));

  private static final Logger LOG = LoggerFactory.getLogger(DuplicateClassesTest.class);

  /**
   * This test finds and logs duplicate classes in the classpath. Such duplicates appear for example
   * when some libraries repackage standard functionality or APIs like javax.* or jakarta.* or when
   * providers change their Maven GAV without changing the internal package structure. In both cases
   * the dependency management can't identify the duplication.
   *
   * <p>This approach of finding duplicates is inspired by <a
   * href="https://stackoverflow.com/a/52639079">Stackoverflow</a>
   */
  @Test
  void checkForDuplicateClasses() {
    int numberOfDuplicates = 0;
    try (ScanResult scanResult = new ClassGraph().scan()) {
      ResourceList allResourcesInClasspath = scanResult.getAllResources();
      ResourceList classFilesInClasspath =
          allResourcesInClasspath
              .filter(resource -> !resource.getURL().toString().contains("/.gradle/wrapper/"))
              .classFilesOnly();
      for (Map.Entry<String, ResourceList> duplicate : classFilesInClasspath.findDuplicatePaths()) {
        if ("module-info.class".equals(duplicate.getKey())) {
          continue;
        }
        if (ignorePatterns.stream().anyMatch(p -> p.matcher(duplicate.getKey()).matches())) {
          continue;
        }
        LOG.warn("Class files path: {}", duplicate.getKey()); // Classfile path
        numberOfDuplicates++;
        for (Resource res : duplicate.getValue()) {
          LOG.warn(" -> {}", res.getURL()); // Resource URL, showing classpath element
        }
      }
      LOG.warn("Found {} duplicates.", numberOfDuplicates);
      assertThat(numberOfDuplicates)
          .describedAs("expecting no duplicate classes but found %s", numberOfDuplicates)
          .isZero();
    }
  }
}
