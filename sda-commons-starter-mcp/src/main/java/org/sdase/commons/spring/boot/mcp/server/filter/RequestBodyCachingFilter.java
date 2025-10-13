/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * A filter that wraps incoming {@link HttpServletRequest} to allow multiple reads of the request
 * body. This is useful for scenarios where the request body needs to be accessed by multiple
 * components, such as logging or authentication filters. The filter caches the request body to
 * enable repeated access.
 *
 * <p>This filter runs with the highest precedence to ensure the body is cached before other
 * filters, such as Spring Security, process the request.
 */
public class RequestBodyCachingFilter extends OncePerRequestFilter {

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    MultipleReadHttpRequest wrappedRequest = new MultipleReadHttpRequest(request);
    // Force reading the body to cache it
    wrappedRequest.getInputStream().readAllBytes();
    filterChain.doFilter(wrappedRequest, response);
  }
}
