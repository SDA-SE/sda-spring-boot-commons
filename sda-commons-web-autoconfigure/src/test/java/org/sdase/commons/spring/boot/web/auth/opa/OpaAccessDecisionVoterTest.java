/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.opa;

// TODO update to opentelemetry
// import io.opentracing.Tracer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.auth.AuthTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.FilterInvocation;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest(classes = AuthTestApp.class)
@ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
class OpaAccessDecisionVoterTest {

  @Value("${opa.base.url}")
  private String opaBaseUrl;

  @Autowired private AuthMock authMock;
  @Autowired private OpaRequestBuilder opaRequestBuilder;

  // TODO update to opentelemetry
  // @Autowired private Tracer tracer;

  @Autowired
  @Qualifier("opaRestTemplate")
  private RestTemplate opaRestTemplate;

  @Autowired private ApplicationContext applicationContext;

  @Test
  void shouldAllowAnonymousIfDisabled() {
    var disabledDecisionVoter =
        new OpaAccessDecisionVoter(
            true,
            opaBaseUrl,
            "",
            opaRequestBuilder,
            opaRestTemplate,
            applicationContext,
            null /* TODO update to opentelemetry*/);
    var requestMock = mock(HttpServletRequest.class);
    when(requestMock.getUserPrincipal()).thenReturn(null);
    var filterInvocationMock = mock(FilterInvocation.class);
    when(filterInvocationMock.getHttpRequest()).thenReturn(requestMock);
    assertThat(
            disabledDecisionVoter.vote(mock(Authentication.class), filterInvocationMock, List.of()))
        .isEqualTo(AccessDecisionVoter.ACCESS_GRANTED);
  }

  @Test
  void shouldAllowPrincipalIfDisabled() {
    var disabledDecisionVoter =
        new OpaAccessDecisionVoter(
            true,
            opaBaseUrl,
            "",
            opaRequestBuilder,
            opaRestTemplate,
            applicationContext,
            null /* TODO update to opentelemetry*/);
    var requestMock = mock(HttpServletRequest.class);
    final JwtAuthenticationToken authenticationTokenMock =
        new JwtAuthenticationToken(mock(Jwt.class));
    when(requestMock.getUserPrincipal()).thenReturn(authenticationTokenMock);
    var filterInvocationMock = mock(FilterInvocation.class);
    when(filterInvocationMock.getHttpRequest()).thenReturn(requestMock);
    assertThat(
            disabledDecisionVoter.vote(mock(Authentication.class), filterInvocationMock, List.of()))
        .isEqualTo(AccessDecisionVoter.ACCESS_GRANTED);
  }

  @Test
  void shouldDerivePolicyPackageFromApplicationClass() {
    var decisionVoter =
        new OpaAccessDecisionVoter(
            false,
            opaBaseUrl,
            "",
            opaRequestBuilder,
            opaRestTemplate,
            applicationContext,
            null /* TODO update to opentelemetry*/);
    assertThat(decisionVoter)
        .extracting("opaRequestUrl")
        .asString()
        .isEqualTo(opaBaseUrl + "/v1/data/org/sdase/commons/spring/boot/web/auth");
  }

  @Test
  void shouldUseConfiguredPolicyPackage() {
    var decisionVoter =
        new OpaAccessDecisionVoter(
            false,
            opaBaseUrl,
            "com.example.package",
            opaRequestBuilder,
            opaRestTemplate,
            applicationContext,
            null /* TODO update to opentelemetry*/);
    assertThat(decisionVoter)
        .extracting("opaRequestUrl")
        .asString()
        .isEqualTo(opaBaseUrl + "/v1/data/com/example/package");
  }

  @Test
  void shouldNotVoteIfRequestFails() {
    var restTemplateMock =
        mock(
            RestTemplate.class,
            invocation -> {
              throw new ResourceAccessException("Simulate failure");
            });
    var decisionVoter =
        new OpaAccessDecisionVoter(
            false,
            opaBaseUrl,
            "",
            opaRequestBuilder,
            restTemplateMock,
            applicationContext,
            null /* TODO update to opentelemetry*/);
    var requestMock = mock(HttpServletRequest.class);
    when(requestMock.getUserPrincipal()).thenReturn(null);
    when(requestMock.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    var filterInvocationMock = mock(FilterInvocation.class);
    when(filterInvocationMock.getHttpRequest()).thenReturn(requestMock);

    var vote = decisionVoter.vote(null, filterInvocationMock, List.of());

    assertThat(vote).isEqualTo(AccessDecisionVoter.ACCESS_ABSTAIN);
  }

  @Test
  void shouldNotVoteIfAllowFalse() {
    var decisionVoter =
        new OpaAccessDecisionVoter(
            false,
            opaBaseUrl,
            "",
            opaRequestBuilder,
            opaRestTemplate,
            applicationContext,
            null /* TODO update to opentelemetry*/);
    var requestMock = mock(HttpServletRequest.class);
    when(requestMock.getUserPrincipal()).thenReturn(null);
    when(requestMock.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    var filterInvocationMock = mock(FilterInvocation.class);
    when(filterInvocationMock.getHttpRequest()).thenReturn(requestMock);

    authMock.authorizeAnyRequest().deny();

    var vote = decisionVoter.vote(null, filterInvocationMock, List.of());

    assertThat(vote).isEqualTo(AccessDecisionVoter.ACCESS_ABSTAIN);
  }

  @Test
  void shouldGrantAccessIfAllowTrue() {
    var decisionVoter =
        new OpaAccessDecisionVoter(
            false,
            opaBaseUrl,
            "",
            opaRequestBuilder,
            opaRestTemplate,
            applicationContext,
            null /* TODO update to opentelemetry*/);
    var requestMock = mock(HttpServletRequest.class);
    when(requestMock.getUserPrincipal()).thenReturn(null);
    when(requestMock.getHeaderNames()).thenReturn(Collections.emptyEnumeration());
    var filterInvocationMock = mock(FilterInvocation.class);
    when(filterInvocationMock.getHttpRequest()).thenReturn(requestMock);

    authMock.authorizeAnyRequest().allow();

    var vote = decisionVoter.vote(null, filterInvocationMock, List.of());

    assertThat(vote).isEqualTo(AccessDecisionVoter.ACCESS_GRANTED);
  }
}