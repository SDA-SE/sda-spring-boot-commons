/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.headers;

import java.util.List;
import org.springframework.security.web.header.Header;

public interface SdaSecurityHeaders {

  List<Header> getSecurityHeaders();
}
