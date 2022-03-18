/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.starter.auth.testing.DisableSdaAuthInitializer;
import org.sdase.commons.spring.boot.starter.jackson.test.JacksonTestApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(
    classes = JacksonTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"management.server.port=0"})
@ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
class ObjectMapperIT {

  @LocalServerPort private int port;

  @Autowired private TestRestTemplate client;

  @Test
  void shouldUseConfiguredObjectMapperForSerialization() {
    var actual = client.getForObject("http://localhost:" + port + "/api/fixedTime", Object.class);
    assertThat(actual).extracting("time").isEqualTo("2018-11-21T13:16:47Z");
  }
}
