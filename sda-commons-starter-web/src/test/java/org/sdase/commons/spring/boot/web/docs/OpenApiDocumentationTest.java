/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.docs;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.testing.GoldenFileAssertions;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springdoc.webmvc.api.OpenApiWebMvcResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = DocsTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"springdoc.packagesToScan=org.sdase.commons.spring.boot.web.docs"})
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
class OpenApiDocumentationTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @Autowired private OpenApiWebMvcResource openApiWebMvcResource;

  @Test
  void shouldHaveSameOpenApiInRepository() throws IOException {
    // receive the openapi.yaml from your service
    String expected =
        client.getForObject(
            String.format("http://localhost:%s/api/openapi.yaml", port), String.class);

    // specify where you want your file to be stored
    Path filePath = Paths.get("src/test/resources/openapi.yaml");

    GoldenFileAssertions.assertThat(filePath).hasYamlContentAndUpdateGolden(expected);
  }

  @Test
  void shouldGenerateOpenApiWithoutRestCall() throws Exception {
    HttpServletRequest request = mock(HttpServletRequest.class);
    when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080"));

    var bytes = openApiWebMvcResource.openapiYaml(request, "http://localhost:8080", Locale.ENGLISH);
    var expected = new String(bytes, StandardCharsets.UTF_8);

    // specify where you want your file to be stored
    Path filePath = Paths.get("src/test/resources/openapi.yaml");

    GoldenFileAssertions.assertThat(filePath).hasYamlContentAndUpdateGolden(expected);
  }
}
