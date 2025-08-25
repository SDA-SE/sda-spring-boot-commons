/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.filter;

import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.springframework.util.StreamUtils;

public class MultipleReadHttpRequest extends HttpServletRequestWrapper {

  private final byte[] cachedContent;

  /**
   * Constructs a {@code MultipleReadHttpRequest} that wraps the given {@link HttpServletRequest}
   * and caches its input stream content for multiple reads.
   *
   * @param request the original {@link HttpServletRequest} to wrap
   * @throws IOException if an I/O error occurs while reading the request input stream
   */
  public MultipleReadHttpRequest(HttpServletRequest request) throws IOException {
    super(request);
    InputStream requestInputStream = request.getInputStream();
    this.cachedContent = StreamUtils.copyToByteArray(requestInputStream);
  }

  @Override
  public ServletInputStream getInputStream() {
    return new CachedBodyServletInputStream(this.cachedContent);
  }

  @Override
  public BufferedReader getReader() {
    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(this.cachedContent);
    return new BufferedReader(new InputStreamReader(byteArrayInputStream));
  }
}
