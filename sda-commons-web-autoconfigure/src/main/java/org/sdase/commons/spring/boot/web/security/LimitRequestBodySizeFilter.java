/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security;

import java.io.IOException;
import java.util.Enumeration;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.filter.OncePerRequestFilter;

// based on this hint: https://stackoverflow.com/a/38611154
@Component
public class LimitRequestBodySizeFilter extends OncePerRequestFilter {

  private final DataSize requestBodyMaxSize;

  public LimitRequestBodySizeFilter(
      @Value("${request.body.max.size}") DataSize requestBodyMaxSize) {
    this.requestBodyMaxSize = requestBodyMaxSize;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if (request.getContentLengthLong() > requestBodyMaxSize.toBytes()) {
      response.sendError(HttpStatus.PAYLOAD_TOO_LARGE.value()); // 413
    } else if (isChunkedEncoding(request.getHeaders(HttpHeaders.TRANSFER_ENCODING))) {
      response.sendError(HttpStatus.LENGTH_REQUIRED.value()); // 411
    } else {
      filterChain.doFilter(request, response);
    }
  }

  private boolean isChunkedEncoding(Enumeration<String> transferEncodingHeaders) {
    while (transferEncodingHeaders.hasMoreElements()) {
      var transferEncoding = transferEncodingHeaders.nextElement();
      var normalizedTransferEncoding = transferEncoding.toLowerCase();
      if (normalizedTransferEncoding.contains("chunked")) {
        return true;
      }
    }
    return false;
  }
}
