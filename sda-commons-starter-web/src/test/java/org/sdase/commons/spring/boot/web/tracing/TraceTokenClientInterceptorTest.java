/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.tracing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import feign.RequestTemplate;
import java.util.HashSet;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class TraceTokenClientInterceptorTest {

  static final String TRACE_TOKEN_ATTRIBUTE_NAME = "Trace-Token";

  TraceTokenClientInterceptor traceTokenClientInterceptor = new TraceTokenClientInterceptor();

  @BeforeEach
  @AfterEach
  void reset() {
    RequestContextHolder.resetRequestAttributes();
  }

  @Test
  void shouldUseTraceTokenFromRequestContext() {
    var requestAttributes = new ServletRequestAttributes(new MockHttpServletRequest());
    requestAttributes.setAttribute(
        TRACE_TOKEN_ATTRIBUTE_NAME, "dummy-token", RequestAttributes.SCOPE_REQUEST);
    RequestContextHolder.setRequestAttributes(requestAttributes);

    var given = new RequestTemplate();

    traceTokenClientInterceptor.apply(given);

    assertThat(given.headers()).containsEntry("Trace-Token", List.of("dummy-token"));
  }

  @Test
  void shouldCreateNewTraceTokenIfNotInRequestContext() {
    var given = new RequestTemplate();

    assertThatNoException().isThrownBy(() -> traceTokenClientInterceptor.apply(given));

    assertThat(given.headers())
        .containsKey("Trace-Token")
        .extracting("Trace-Token")
        .asInstanceOf(InstanceOfAssertFactories.LIST)
        .hasSize(1)
        .first()
        .asString()
        .isNotBlank();
  }

  @Test
  void shouldCreateNewTraceTokenIfExistingNotAsExpected() {
    var requestAttributes = new ServletRequestAttributes(new MockHttpServletRequest());
    requestAttributes.setAttribute(
        TRACE_TOKEN_ATTRIBUTE_NAME, new Object(), RequestAttributes.SCOPE_REQUEST);
    RequestContextHolder.setRequestAttributes(requestAttributes);

    var given = new RequestTemplate();

    assertThatNoException().isThrownBy(() -> traceTokenClientInterceptor.apply(given));

    assertThat(given.headers())
        .containsKey("Trace-Token")
        .extracting("Trace-Token")
        .asInstanceOf(InstanceOfAssertFactories.LIST)
        .hasSize(1)
        .first()
        .asString()
        .isNotBlank();
  }

  @Test
  void shouldCreateRandomTraceTokenIfNotInRequestContext() {
    var actualTokens = new HashSet<String>();

    for (int i = 0; i < 100; i++) {
      var given = new RequestTemplate();
      assertThatNoException().isThrownBy(() -> traceTokenClientInterceptor.apply(given));
      actualTokens.add(given.headers().get("Trace-Token").stream().findFirst().orElse(null));
    }

    assertThat(actualTokens).hasSize(100).allMatch(StringUtils::isNotBlank);
  }
}
