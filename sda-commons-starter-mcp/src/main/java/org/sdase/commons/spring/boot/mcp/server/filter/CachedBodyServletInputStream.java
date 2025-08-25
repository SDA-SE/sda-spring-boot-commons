/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class CachedBodyServletInputStream extends ServletInputStream {

  private final ByteArrayInputStream cachedBody;

  public CachedBodyServletInputStream(byte[] cachedBodyBytes) {
    this.cachedBody = new ByteArrayInputStream(cachedBodyBytes);
  }

  @Override
  public boolean isFinished() {
    return cachedBody.available() == 0;
  }

  @Override
  public boolean isReady() {
    return true;
  }

  @Override
  public void setReadListener(ReadListener listener) {
    // This implementation does not support non-blocking IO
    throw new UnsupportedOperationException("Async read not supported");
  }

  @Override
  public int read() {
    return cachedBody.read();
  }

  @Override
  public int read(byte[] b, int off, int len) {
    return cachedBody.read(b, off, len);
  }

  @Override
  public int available() {
    return cachedBody.available();
  }

  @Override
  public void close() throws IOException {
    cachedBody.close();
  }
}
