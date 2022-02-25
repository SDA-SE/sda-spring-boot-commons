/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.docs;

import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.responses.ApiResponses;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/org/sdase/commons/spring/docs/defaults.properties")
public class SdaOpenApiCustomizer {

  @Bean
  public OpenApiCustomiser removeServers() {
    return openApi -> openApi.servers(null);
  }

  @Bean
  public OpenApiCustomiser sortResponseCodes() {
    return openApi ->
        openApi.getPaths().values().stream()
            .map(this::getOperation)
            .flatMap(List::stream)
            .forEach(this::sortResponses);
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
        .collect(Collectors.toList());
  }
}
