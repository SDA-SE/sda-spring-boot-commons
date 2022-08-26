/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = ApiExceptionTestApp.class,
    webEnvironment = RANDOM_PORT,
    properties = {"management.server.port=0"})
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
class ApiExceptionHandlerIT {

  @Autowired TestRestTemplate client;

  @LocalServerPort private int port;

  @ParameterizedTest()
  @ArgumentsSource(CustomArgumentProvider.class)
  void shouldThrowApiExceptionWithApiErrorBody(ApiError apiError) {

    var responseCode = HttpStatus.resolve(Integer.parseInt(apiError.getTitle()));

    var response =
        client.postForEntity("http://localhost:" + port + "/api/throw", apiError, ApiError.class);
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(responseCode);
    assertThat(response.getBody()).usingRecursiveComparison().isEqualTo(apiError);
  }

  static class CustomArgumentProvider implements ArgumentsProvider {
    @Override
    public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
      return Stream.of(
          Arguments.of(
              new ApiError(
                  "422", List.of(new ApiInvalidParam("FIELD_1", "INVALID", "INVALID_FIELD")))),
          Arguments.of(
              new ApiError("500", List.of(new ApiInvalidParam("FIELD_1", "WRONG_INPUT", "INPUT")))),
          Arguments.of(
              new ApiError("404", List.of(new ApiInvalidParam("ID", "NOT_FOUND", "NOT_FOUND")))),
          Arguments.of(new ApiError("404")));
    }
  }
}
