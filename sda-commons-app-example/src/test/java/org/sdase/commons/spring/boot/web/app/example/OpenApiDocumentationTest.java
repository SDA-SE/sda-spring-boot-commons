/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.testing.GoldenFileAssertions;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;

/**
 * A test that stores the most recent openapi.yaml in the repository and makes sure it is
 * up-to-date.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"springdoc.packagesToScan=org.sdase.commons.spring.boot.web.app.example"})
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
class OpenApiDocumentationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @Test
  void shouldHaveSameOpenApiInRepository() throws IOException {
    // receive the openapi.yaml from your service
    String expected =
        client.getForObject(
            String.format("http://localhost:%s/api/openapi.yaml", port), String.class);

    // specify where you want your file to be stored
    Path filePath = Paths.get("openapi.yaml").toAbsolutePath();

    GoldenFileAssertions.assertThat(filePath).hasYamlContentAndUpdateGolden(expected);
  }
}
