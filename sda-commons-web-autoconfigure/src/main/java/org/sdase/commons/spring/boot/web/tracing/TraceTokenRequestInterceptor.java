/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.tracing;

import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;

public class TraceTokenRequestInterceptor implements HandlerInterceptor {

  public static final String TRACE_TOKEN_HEADER_NAME = "Trace-Token";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    var traceToken = computeTraceTokenIfAbsent(request.getHeader(TRACE_TOKEN_HEADER_NAME));

    response.setHeader(TRACE_TOKEN_HEADER_NAME, traceToken);
    RequestContextHolder.currentRequestAttributes()
        .setAttribute(TRACE_TOKEN_HEADER_NAME, traceToken, RequestAttributes.SCOPE_REQUEST);

    return true;
  }

  private String computeTraceTokenIfAbsent(String traceToken) {
    if (null != traceToken && !traceToken.isBlank()) {
      return traceToken;
    }

    return UUID.randomUUID().toString();
  }
}
