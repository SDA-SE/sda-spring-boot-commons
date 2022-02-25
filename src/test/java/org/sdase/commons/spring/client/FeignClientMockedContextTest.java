/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.client;

import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.auth.testing.DisableSdaAuthInitializer;
import org.sdase.commons.spring.client.test.ClientTestApp;
import org.sdase.commons.spring.client.test.ClientTestConstraints;
import org.sdase.commons.spring.client.test.OtherServiceAuthenticatedClient;
import org.sdase.commons.spring.client.test.OtherServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest(classes = ClientTestApp.class)
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
@AutoConfigureMockMvc
class FeignClientMockedContextTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private OtherServiceClient otherServiceClient;

  @MockBean private OtherServiceAuthenticatedClient otherServiceAuthenticatedClient;

  @MockBean private ClientTestConstraints clientTestConstraints;

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
