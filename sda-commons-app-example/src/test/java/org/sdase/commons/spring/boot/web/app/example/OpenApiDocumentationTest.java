/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

// ATTENTION: The source of this class is included in the public documentation.

import java.io.IOException;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.testing.GoldenFileAssertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "springdoc.packagesToScan=org.sdase.commons.spring.boot.web.app.example",
      "management.server.port=0"
    })
@AutoConfigureTestRestTemplate
class OpenApiDocumentationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @Test
  void shouldHaveSameOpenApiInRepository() throws IOException {
    var expectedOpenApi =
        client.getForObject(
            String.format("http://localhost:%s/api/openapi.yaml", port), String.class);

    var actualOpenApiInRepository = Paths.get("openapi.yaml").toAbsolutePath();

    GoldenFileAssertions.assertThat(actualOpenApiInRepository)
        .hasYamlContentAndUpdateGolden(expectedOpenApi);
  }
}
