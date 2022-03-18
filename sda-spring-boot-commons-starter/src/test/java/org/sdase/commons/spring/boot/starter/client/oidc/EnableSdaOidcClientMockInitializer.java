/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.client.oidc;

import com.github.tomakehurst.wiremock.WireMockServer;
import java.util.HashMap;
import java.util.Map;
import org.sdase.commons.spring.boot.starter.auth.testing.AuthMock;
import org.sdase.commons.spring.boot.starter.auth.testing.EnableSdaAuthMockInitializer;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

/**
 * To be used on test classes with {@link org.springframework.boot.test.context.SpringBootTest} as *
 * {@linkplain ContextConfiguration#initializers() initializer} in a {@link ContextConfiguration} to
 * create a mock for an OIDC provider.
 *
 * <p>When enabled, a {@link AuthMock} is registered in the application context. The {@code
 * AuthMock} can be {@link org.springframework.beans.factory.annotation.Autowired} in the test class
 * to generate accepted JSON Web Tokens.
 *
 * <p>Example:
 *
 * <pre>
 *   <code>{@literal @}SpringBootTest(
 *        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
 *        properties={"feign.test.api.base.url=http://localhost:${wiremock.server.port}"}
 *    )
 *   {@literal @}ContextConfiguration(initializers = EnableSdaOidcClientMockInitializer.class)
 *   {@literal @}AutoConfigureWireMock(port = 0)
 *    class MyTest {
 *      {@literal @}Autowired private AuthMock authMock;
 *      {@literal @}LocalServerPort private int port;
 *
 *      {@literal @}Test
 *        void shouldUseAccessToken() {
 *          stubFor(
 *            get("/hello")
 *                 .withHeader(AUTHORIZATION, equalTo("Bearer " + authMock.providedAccessToken()))
 *                .willReturn(ok()));
 *
 *          testClient.getSomething();
 *
 *          verify(
 *             getRequestedFor(urlMatching("/hello"))
 *                 .withHeader(AUTHORIZATION, equalTo("Bearer " +  authMock.providedAccessToken())));
 *   }
 *   </code>
 * </pre>
 */
@Configuration
public class EnableSdaOidcClientMockInitializer extends EnableSdaAuthMockInitializer {

  @Override
  public Map<String, String> getTestProperties(WireMockServer wireMockServer) {
    var testProperties = new HashMap<>(super.getTestProperties(wireMockServer));
    testProperties.putAll(
        Map.of(
            "oidc.client.issuer.uri",
            String.format("http://localhost:%d/issuer", wireMockServer.port()),
            "oidc.client.id",
            "oidcClient",
            "oidc.client.secret",
            "s3cret"));
    return testProperties;
  }
}
