/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.sdase.commons.spring.boot.mcp.server.TestApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;

@SpringBootTest(
    classes = TestApplication.class,
    webEnvironment = RANDOM_PORT,
    properties = {"auth.disable=true", "opa.disable=true"})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthenticationDisabledIT {
  @LocalServerPort private int port;

  private WebClient webClient;
  private Flux<String> eventStream;
  private Disposable subscription;
  private BlockingQueue<String> eventQueue = new LinkedBlockingQueue<>();
  private String messageEndpoint;

  @BeforeAll
  void setup() throws InterruptedException {
    webClient = WebClient.builder().baseUrl("http://localhost:" + port).build();

    eventStream =
        webClient
            .get()
            .uri("/sse")
            .accept(MediaType.TEXT_EVENT_STREAM)
            .header("Cache-Control", "no-cache")
            .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .retrieve()
            .bodyToFlux(String.class);

    // Subscribe once, push all events into queue
    subscription = eventStream.subscribe(eventQueue::offer);

    String event = eventQueue.poll(5, TimeUnit.SECONDS);
    assertThat(event).isNotNull().contains("/mcp/message");
    messageEndpoint = event;
  }

  @AfterAll
  void tearDown() {
    subscription.dispose();
  }

  @Test
  @Order(1)
  void shouldInitialize() {

    var mcpClient = WebClient.builder().baseUrl("http://localhost:" + port).build();
    var response =
        mcpClient
            .post()
            .uri(messageEndpoint)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.TEXT_EVENT_STREAM)
            .header("Cache-Control", "no-cache")
            .bodyValue(
                """
              {
                "jsonrpc": "2.0",
                "method": "initialize",
                "id": "0",
                "params": {
                  "protocolVersion": "2024-11-05",
                  "capabilities": {},
                  "clientInfo": {
                    "name": "Java SDK MCP Client",
                    "version": "1.0.0"
                  }
                }
              }
              """)
            .retrieve()
            .toBodilessEntity()
            .block();

    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
