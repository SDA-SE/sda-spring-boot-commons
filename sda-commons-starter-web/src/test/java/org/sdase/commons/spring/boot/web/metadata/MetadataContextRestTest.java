/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata;

import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.client.WireMock;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.sdase.commons.spring.boot.metadata.context.DetachedMetadataContext;
import org.sdase.commons.spring.boot.metadata.context.MetadataContext;
import org.sdase.commons.spring.boot.web.metadata.test.MetadataTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

/** Test that http client configuration is correct. */
@SetSystemProperty(key = "METADATA_FIELDS", value = "tenant-id")
@SpringBootTest(
    classes = {MetadataTestApp.class, MetadataContextConfiguration.class},
    webEnvironment = RANDOM_PORT,
    // verify authentication in this test, not authorization
    properties = {
      "opa.disable=true",
      "metadata.other.baseUrl=http://localhost:${wiremock.server.port}/api",
      "metadata.otherAuthenticated.baseUrl=http://localhost:${wiremock.server.port}/api"
    })
@ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
@AutoConfigureWireMock(port = 0)
@AutoConfigureMockMvc
class MetadataContextRestTest {

  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    WireMock.reset();
  }

  @Test
  void submitMetadataContextInPlatformClient() throws Exception {
    stubFor(
        get("/api/metadata-auth-hello").withHeader("tenant-id", equalTo("t-1")).willReturn(ok()));
    var httpHeaders = new HttpHeaders();
    httpHeaders.put("tenant-id", List.of("t-1"));

    mockMvc
        .perform(
            MockMvcRequestBuilders.request(HttpMethod.GET, "/metadataAuthProxy")
                .headers(httpHeaders)
                .accept(APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk());

    WireMock.verify(
        WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/metadata-auth-hello"))
            .withHeader("tenant-id", WireMock.equalTo("t-1")));
  }

  @Test
  void dontSubmitMetadataContextInExternalClient() throws Exception {
    DetachedMetadataContext metadataContext = new DetachedMetadataContext();
    metadataContext.put("tenant-id", List.of("t-1"));
    MetadataContext.createContext(metadataContext);

    var httpHeaders = new HttpHeaders();
    httpHeaders.put("tenant-id", List.of("t-1"));

    stubFor(get("/api/metadata-hello").willReturn(ok()));

    mockMvc
        .perform(
            MockMvcRequestBuilders.request(HttpMethod.GET, "/metadataProxy")
                .headers(httpHeaders)
                .accept(APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk());

    WireMock.verify(
        WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/metadata-hello"))
            .withoutHeader("tenant-id"));
  }

  @Test
  void submitMetadataContextInPlatformAsyncClient() throws Exception {
    stubFor(
        get("/api/metadata-auth-hello")
            .withHeader("tenant-id", equalTo("t-1"))
            .willReturn(ResponseDefinitionBuilder.okForJson(Map.of("hello", "world"))));
    var httpHeaders = new HttpHeaders();
    httpHeaders.put("tenant-id", List.of("t-1"));

    mockMvc
        .perform(
            MockMvcRequestBuilders.request(HttpMethod.GET, "/metadataAuthProxyAsync")
                .headers(httpHeaders)
                .accept(APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk());

    WireMock.verify(
        100,
        WireMock.getRequestedFor(WireMock.urlPathEqualTo("/api/metadata-auth-hello"))
            .withHeader("tenant-id", WireMock.equalTo("t-1")));
  }
}
