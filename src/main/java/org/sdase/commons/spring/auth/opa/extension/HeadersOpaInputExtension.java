/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.auth.opa.extension;

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
