/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.app.example;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
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

  // load only once, because servers are readded in subsequent requests
  private static String openapi;

  // verification of the current OpenAPI

  @Test
  void shouldHaveSameOpenApiInRepository() throws IOException {
    // receive the openapi.yaml from your service
    var expected = loadRawOpenApiAsYaml();

    // specify where you want your file to be stored
    var filePath = Paths.get("openapi.yaml").toAbsolutePath();

    GoldenFileAssertions.assertThat(filePath).hasYamlContentAndUpdateGolden(expected);
  }

  // integration testing of the OpenAPI customisation

  @Test
  void shouldNotContainServers() throws JsonProcessingException {
    var openapi = loadRawOpenApiAsYaml();
    var actual = YAMLMapper.builder().build().readValue(openapi, Object.class);
    assertThat(actual)
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsEntry("openapi", "3.0.1")
        .doesNotContainKey("servers");
  }

  @Test
  void shouldSortResponsesByCode() throws JsonProcessingException {
    var openapi = loadRawOpenApiAsYaml();
    var actual = YAMLMapper.builder().build().readValue(openapi, Object.class);
    assertThat(actual)
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .containsEntry("openapi", "3.0.1")
        .extractingByKey("paths")
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .extractingByKey("/myResource")
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .extractingByKey("get")
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .extractingByKey("responses")
        .asInstanceOf(InstanceOfAssertFactories.MAP)
        .extracting(Map::keySet)
        .extracting(ArrayList::new)
        .asList()
        .containsExactly("200", "404");
  }

  private String loadRawOpenApiAsYaml() {
    if (openapi == null) {
      openapi =
          client.getForObject(
              String.format("http://localhost:%s/api/openapi.yaml", port), String.class);
    }
    return openapi;
  }
}
