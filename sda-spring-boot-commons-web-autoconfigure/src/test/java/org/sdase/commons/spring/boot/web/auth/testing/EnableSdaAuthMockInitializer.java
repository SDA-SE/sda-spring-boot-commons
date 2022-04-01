/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.auth.testing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import java.util.Map;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.test.context.ContextConfiguration;

/**
 * To be used on test classes with {@link org.springframework.boot.test.context.SpringBootTest} as *
 * {@linkplain ContextConfiguration#initializers() initializer} in a {@link ContextConfiguration} to
 * create a mock for an OIDC provider.
 *
 * <p>When enabled, a {@link AuthMock} is registered in the application context. The {@code
 * AuthMock} can be {@link org.springframework.beans.factory.annotation.Autowired} in the test class
 * to generate accepted JSON Web Tokens. The application is configured accordingly when using {@link
 * org.sdase.commons.spring.boot.web.auth.EnableSdaSecurity}.
 *
 * <p>Example:
 *
 * <pre>
 *   <code>{@literal @}SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 *   {@literal @}ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
 *    class MyTest {
 *      {@literal @}Autowired private AuthMock authMock;
 *      {@literal @}LocalServerPort private int port;
 *
 *      {@literal @}Test
 *      void shouldProceedWhenAuthorized() {
 *        authMock.authorizeAnyRequest().allow();
 *        // …
 *      }
 *      {@literal @}Test
 *      void shouldBeAuthenticated() {
 *        var actual = authMock
 *              .authentication()
 *              .authenticatedClient()
 *              .getForObject(String.format("http://localhost:%d/api/resources", port), Object.class);
 *        // …
 *      }
 *    }</code>
 * </pre>
 */
@Configuration
public class EnableSdaAuthMockInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {

  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    WireMockServer wireMockServer = new WireMockServer(new WireMockConfiguration().dynamicPort());
    wireMockServer.start();

    applicationContext
        .getBeanFactory()
        .registerSingleton("authMock", new AuthMock(wireMockServer, new ObjectMapper()));

    applicationContext.addApplicationListener(
        applicationEvent -> {
          if (applicationEvent instanceof ContextClosedEvent) {
            wireMockServer.stop();
          }
        });

    TestPropertyValues.of(getTestProperties(wireMockServer)).applyTo(applicationContext);
  }

  public Map<String, String> getTestProperties(WireMockServer wireMockServer) {
    return Map.of(
        "auth.issuers",
        String.format("http://localhost:%d/issuer", wireMockServer.port()),
        "opa.client.timeout",
        "1500ms",
        "opa.client.connection.timeout",
        "1500ms",
        "opa.base.url",
        String.format("http://localhost:%d", wireMockServer.port()));
  }
}
