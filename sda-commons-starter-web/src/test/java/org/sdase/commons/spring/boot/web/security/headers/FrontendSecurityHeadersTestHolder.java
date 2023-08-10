/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.headers;

import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.sdase.commons.spring.boot.web.security.test.SecurityTestApp;
import org.sdase.commons.spring.boot.web.testing.auth.AuthMock;
import org.sdase.commons.spring.boot.web.testing.auth.DisableSdaAuthInitializer;
import org.sdase.commons.spring.boot.web.testing.auth.EnableSdaAuthMockInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

/** This test class depends on the {@code @EnableFrontendSecurity} annotation being present. */
@EnableFrontendSecurity
abstract class FrontendSecurityHeadersTestHolder extends AbstractSecurityHeadersTest {

  @SpringBootTest(
      classes = SecurityTestApp.class,
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  @ContextConfiguration(initializers = EnableSdaAuthMockInitializer.class)
  static class AuthEnabledTest extends FrontendSecurityHeadersTestHolder {

    @Autowired AuthMock authMock;

    @BeforeEach
    void allow() {
      authMock.authorizeAnyRequest().allow();
    }
  }

  @SpringBootTest(
      classes = SecurityTestApp.class,
      webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  @ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
  static class AuthDisabledTest extends FrontendSecurityHeadersTestHolder {}

  @Override
  protected Stream<Arguments> predefinedSecurityHeaders() {
    return AbstractSecurityHeadersTest.predefinedFrontendSecurityHeaders();
  }

  @ParameterizedTest
  @MethodSource("predefinedFrontendSecurityHeaders")
  void shouldAddSecurityHeaders(String predefinedHeaderName, String expectedPredefinedHeaderValue) {
    super.shouldAddSecurityHeaders(predefinedHeaderName, expectedPredefinedHeaderValue);
  }

  @ParameterizedTest
  @MethodSource("predefinedFrontendSecurityHeaders")
  void shouldAllowOverwritingSecurityHeaders(String predefinedHeaderName) {
    super.shouldAllowOverwritingHeaders(predefinedHeaderName);
  }
}
