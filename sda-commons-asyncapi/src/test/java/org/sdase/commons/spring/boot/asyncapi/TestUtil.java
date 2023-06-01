/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.asyncapi;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TestUtil {

  private TestUtil() {
    // No public constructor
  }

  static String readResource(String filename) throws URISyntaxException, IOException {
    return new String(Files.readAllBytes(Paths.get(TestUtil.class.getResource(filename).toURI())));
  }
}
