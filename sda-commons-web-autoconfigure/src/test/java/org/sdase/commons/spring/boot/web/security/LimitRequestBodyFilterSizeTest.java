/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.error.ApiError;
import org.sdase.commons.spring.boot.web.security.test.CreateSomethingResource;
import org.sdase.commons.spring.boot.web.security.test.SecurityTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = SecurityTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
class LimitRequestBodyFilterSizeTest {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @Test
  void shouldNotAcceptLargePostRequests() {
    var items = new ArrayList<String>();
    // will be 1MB + JSON characters
    for (int i = 0; i < 1024; i++) {
      items.add(createOneKiloByte());
    }
    var given = new CreateSomethingResource().setItems(items);
    var url = getServerBaseUrl() + "/api/createSomething";
    var actual = client.postForEntity(url, given, ApiError.class);
    assertThat(actual)
        .extracting(ResponseEntity::getStatusCode, r -> r.getHeaders().getContentType())
        .contains(HttpStatus.PAYLOAD_TOO_LARGE, MediaType.APPLICATION_JSON);
    assertThat(actual.getBody()).extracting(ApiError::getTitle).asString().isNotBlank();
  }

  @Test
  void shouldAcceptSmallPostRequests() {
    var given =
        new CreateSomethingResource().setItems(List.of(createOneKiloByte(), createOneKiloByte()));
    var url = getServerBaseUrl() + "/api/createSomething";
    var location = client.postForLocation(url, given);
    assertThat(location).isEqualTo(URI.create(getServerBaseUrl() + "/api/2"));
  }

  @Test
  void shouldNotAcceptPostRequestsWithoutContentLength() throws IOException {
    var given =
        new ObjectMapper()
            .writeValueAsBytes(
                new CreateSomethingResource()
                    .setItems(List.of(createOneKiloByte(), createOneKiloByte())));
    var url = getServerBaseUrl() + "/api/createSomething";

    var urlConnection = (HttpURLConnection) new URL(url).openConnection();
    try {
      urlConnection.setRequestMethod("POST");
      urlConnection.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
      urlConnection.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
      urlConnection.setChunkedStreamingMode(0);
      urlConnection.setDoInput(true);
      urlConnection.setDoOutput(true);
      urlConnection.connect();
      try (var os = urlConnection.getOutputStream()) {
        os.write(given);
        os.flush();
      }
      assertThat(urlConnection.getResponseCode()).isEqualTo(411);
      assertThatExceptionOfType(IOException.class).isThrownBy(urlConnection::getInputStream);
      try (var is = urlConnection.getErrorStream()) {
        assertThat(is).isNotNull();
        var apiError = new ObjectMapper().readValue(is, ApiError.class);
        assertThat(apiError).extracting(ApiError::getTitle).isNotNull();
      }
    } finally {
      urlConnection.disconnect();
    }
  }

  String getServerBaseUrl() {
    return String.format("http://localhost:%s", port);
  }

  String createOneKiloByte() {
    return RandomStringUtils.random(1024, true, true);
  }
}
