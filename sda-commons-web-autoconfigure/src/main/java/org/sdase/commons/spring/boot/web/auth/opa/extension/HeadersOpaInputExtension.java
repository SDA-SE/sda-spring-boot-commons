/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.opa.extension;

import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

@Component
public class HeadersOpaInputExtension implements OpaInputExtension<MultiValueMap<String, String>> {

  @Override
  public MultiValueMap<String, String> createAdditionalInputContent(HttpServletRequest request) {
    var headersForInput = new LinkedMultiValueMap<String, String>();
    var headerNames = request.getHeaderNames();
    while (headerNames.hasMoreElements()) {
      var rawHeaderName = headerNames.nextElement();
      var normalizedHeaderName = normalizeHeaderName(rawHeaderName);
      request
          .getHeaders(normalizedHeaderName)
          .asIterator()
          .forEachRemaining(v -> headersForInput.add(normalizedHeaderName, v));
    }
    return headersForInput;
  }

  private String normalizeHeaderName(String rawHeaderName) {
    return rawHeaderName.toLowerCase();
  }
}
