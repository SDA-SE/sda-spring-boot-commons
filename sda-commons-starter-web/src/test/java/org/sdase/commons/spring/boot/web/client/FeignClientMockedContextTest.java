/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.client.test.ClientTestApp;
import org.sdase.commons.spring.boot.web.client.test.ClientTestConstraints;
import org.sdase.commons.spring.boot.web.client.test.OtherServiceAuthenticatedClient;
import org.sdase.commons.spring.boot.web.client.test.OtherServiceClient;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(
    classes = ClientTestApp.class,
    properties = {
      "other.baseUrl=http://localhost:${wiremock.server.port}/api",
      "otherAuthenticated.baseUrl=http://localhost:${wiremock.server.port}/api",
    })
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
@AutoConfigureMockMvc
class FeignClientMockedContextTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private OtherServiceClient otherServiceClient;

  @MockitoBean private OtherServiceAuthenticatedClient otherServiceAuthenticatedClient;

  @MockitoBean private ClientTestConstraints clientTestConstraints;

  @Test
  void shouldCallOtherServiceWithoutAuthentication() throws Exception {
    when(otherServiceClient.getSomething()).thenReturn(Map.of("hello", "world"));

    mockMvc
        .perform(MockMvcRequestBuilders.request(HttpMethod.GET, "/proxy").accept(APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.hello").value("world"));
  }

  @Test
  void shouldCallAsyncWithAuthentication() throws Exception {

    when(otherServiceAuthenticatedClient.getSomething()).thenReturn(Map.of("hello", "world"));
    when(clientTestConstraints.isCallAsyncAllowed()).thenReturn(true);

    mockMvc
        .perform(
            MockMvcRequestBuilders.request(HttpMethod.GET, "/authProxyAsync")
                .accept(APPLICATION_JSON))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
        .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(100));
  }
}
