/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.logging;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class HttpRequestLoggingInterceptor implements HandlerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(HttpRequestLoggingInterceptor.class);

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    return true;
  }

  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {

    try (var protocolIgnored = MDC.putCloseable("protocol", request.getProtocol());
        var methodIgnored = MDC.putCloseable("method", request.getMethod());
        var contentLengthIgnored =
            MDC.putCloseable("contentLength", String.valueOf(request.getContentLength()));
        var userAgentIgnored = MDC.putCloseable("userAgent", request.getHeader("user-agent"));
        var uriIgnored =
            MDC.putCloseable("uri", request.getContextPath() + request.getServletPath());
        var remoteAddressIgnored = MDC.putCloseable("remoteAddress", request.getRemoteAddr());
        var statusIgnored = MDC.putCloseable("status", String.valueOf(response.getStatus()));
        var requestTraceTokenIgnored =
            MDC.putCloseable("requestHeaderTraceToken", request.getHeader("trace-token"));
        var responseTraceTokenIgnored =
            MDC.putCloseable("responseHeaderTraceToken", response.getHeader("Trace-Token")); ) {
      logger.info(
          "{} {}{} {} - {}",
          request.getMethod(),
          request.getContextPath(),
          request.getServletPath(),
          response.getStatus(),
          request.getHeader("user-agent"));
    }
  }
}
