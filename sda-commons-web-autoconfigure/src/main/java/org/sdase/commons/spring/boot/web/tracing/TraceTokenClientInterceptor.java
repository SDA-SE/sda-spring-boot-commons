/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.tracing;

import static org.sdase.commons.spring.boot.web.tracing.TraceTokenRequestInterceptor.TRACE_TOKEN_HEADER_NAME;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.UUID;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class TraceTokenClientInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate template) {
    var traceToken = computeTraceTokenIfAbsent();
    template.header(TRACE_TOKEN_HEADER_NAME, traceToken);
  }

  private String computeTraceTokenIfAbsent() {
    var attribute =
        RequestContextHolder.currentRequestAttributes()
            .getAttribute(TRACE_TOKEN_HEADER_NAME, RequestAttributes.SCOPE_REQUEST);

    if (attribute instanceof String strAttribute) {
      RequestContextHolder.currentRequestAttributes()
          .setAttribute(TRACE_TOKEN_HEADER_NAME, strAttribute, RequestAttributes.SCOPE_REQUEST);
      return strAttribute;
    }

    return UUID.randomUUID().toString();
  }
}
