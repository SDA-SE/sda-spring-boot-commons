/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.cacertificates.ssl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class TestUtil {
  private TestUtil() {
    // utils class
  }

  public static String readPemContent(String pemResource) {
    try {
      return new String(
          CompositeX509TrustManagerTest.class
              .getClassLoader()
              .getResourceAsStream(pemResource)
              .readAllBytes(),
          StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new UncheckedIOException(String.format("Failed to read %s", pemResource), e);
    }
  }
}
