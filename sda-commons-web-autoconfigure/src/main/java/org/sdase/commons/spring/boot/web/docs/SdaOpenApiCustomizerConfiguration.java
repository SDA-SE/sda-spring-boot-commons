/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.docs;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.PropertySource;

@AutoConfiguration
@PropertySource("classpath:/org/sdase/commons/spring/boot/web/docs/defaults.properties")
public class SdaOpenApiCustomizerConfiguration {

  @Bean
  public OpenApiCustomiser removeServers() {
    return openApi -> openApi.servers(null);
  }

  @Bean
  public OpenApiCustomiser sortResponses() {
    return openApi ->
        openApi.getPaths().values().stream()
            .map(this::getOperation)
            .flatMap(List::stream)
            .forEach(this::sortResponses);
  }

  @Bean
  public OpenApiCustomiser sortPaths() {
    return openApi -> openApi.setPaths(sortPaths(openApi.getPaths()));
  }

  @Bean
  public OpenApiCustomiser sortComponents() {
    return openApi -> openApi.setComponents(sortComponents(openApi.getComponents()));
  }

  private Components sortComponents(Components components) {
    if (components == null) {
      return null;
    }

    components.setSchemas(sortSchemas(components.getSchemas()));

    components.setResponses(createSorted(components.getResponses()));
    components.setParameters(createSorted(components.getParameters()));
    components.setExamples(createSorted(components.getExamples()));
    components.setRequestBodies(createSorted(components.getRequestBodies()));
    components.setHeaders(createSorted(components.getHeaders()));
    components.setSecuritySchemes(createSorted(components.getSecuritySchemes()));
    components.setLinks(createSorted(components.getLinks()));
    components.setCallbacks(createSorted(components.getCallbacks()));
    components.setExtensions(createSorted(components.getExtensions()));

    return components;
  }

  private Paths sortPaths(Paths paths) {
    if (paths == null) {
      return null;
    }

    TreeMap<String, PathItem> sorted = new TreeMap<>(paths);
    paths.clear();
    paths.putAll(sorted);
    return paths;
  }

  private void sortResponses(Operation operation) {
    operation.responses(
        operation.getResponses().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    Map.Entry::getValue,
                    (left, right) -> left,
                    ApiResponses::new)));
  }

  private List<Operation> getOperation(PathItem pathItem) {
    return Stream.of(
            pathItem.getGet(),
            pathItem.getPut(),
            pathItem.getPost(),
            pathItem.getDelete(),
            pathItem.getOptions(),
            pathItem.getHead(),
            pathItem.getPatch(),
            pathItem.getTrace())
        .filter(Objects::nonNull)
        .toList();
  }

  /** Recursively sort all the schemas in the Map. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  private static SortedMap<String, Schema> sortSchemas(Map<String, Schema> schemas) {
    if (schemas == null) {
      return null;
    }

    TreeMap<String, Schema> sorted = new TreeMap<>();
    schemas.forEach(
        (key, value) -> {
          ((Schema<?>) value).setProperties(sortSchemas(value.getProperties()));
          sorted.put(key, value);
        });

    return sorted;
  }

  /** Created sorted map based on natural key order. */
  private <T> SortedMap<String, T> createSorted(Map<String, T> map) {
    return map == null ? null : new TreeMap<>(map);
  }
}
