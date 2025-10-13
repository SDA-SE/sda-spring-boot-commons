/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth.opa.extension;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Extension that provides request headers to OPA for authorization decisions. */
@Component
public class HeadersOpaInputExtension implements OpaInputExtension<Map<String, List<String>>> {

  @Override
  public String getNamespace() {
    return "headers";
  }

  @Override
  public Map<String, List<String>> createAdditionalInputContent(
      HttpServletRequest httpServletRequest) {
    Map<String, List<String>> headers = new LinkedHashMap<>();
    Collections.list(httpServletRequest.getHeaderNames())
        .forEach(
            headerName ->
                headers.put(
                    headerName.toLowerCase(),
                    Collections.list(httpServletRequest.getHeaders(headerName))));
    return headers;
  }
}
