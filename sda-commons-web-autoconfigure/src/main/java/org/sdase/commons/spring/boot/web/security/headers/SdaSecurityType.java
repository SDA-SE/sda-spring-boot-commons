/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.headers;

import static java.util.Arrays.asList;
import static org.sdase.commons.spring.boot.web.security.headers.SdaSecurityType.Common.WEB_SECURITY_HEADERS;

import java.util.List;
import java.util.stream.Stream;
import org.springframework.security.web.header.Header;

public enum SdaSecurityType {
  RESTFUL_SECURITY(
      Stream.concat(
              WEB_SECURITY_HEADERS.stream(),
              Stream.of(
                  new Header(
                      "Content-Security-Policy",
                      String.join(
                          "; ",
                          asList("default-src 'none'", "frame-ancestors 'none'", "sandbox")))))
          .toList()),

  FRONTEND_SECURITY(
      Stream.concat(
              WEB_SECURITY_HEADERS.stream(),
              Stream.of(
                  new Header(
                      "Content-Security-Policy",
                      String.join(
                          "; ",
                          asList(
                              "default-src 'self'",
                              "script-src 'self'",
                              "img-src 'self'",
                              "style-src 'self'",
                              "font-src 'self'",
                              "frame-src 'none'",
                              "object-src 'none'")))))
          .toList());

  private final List<Header> headers;

  SdaSecurityType(List<Header> headers) {
    this.headers = headers;
  }

  public List<Header> headers() {
    return headers;
  }

  static class Common {
    private Common() {
      // just to hold WEB_SECURITY_HEADERS
    }

    static final List<Header> WEB_SECURITY_HEADERS =
        List.of(
            new Header("X-Frame-Options", "DENY"),
            new Header("X-Content-Type-Options", "nosniff"),
            new Header("X-XSS-Protection", "1; mode=block"),
            new Header("Referrer-Policy", "same-origin"),
            new Header("X-Permitted-Cross-Domain-Policies", "none"));
  }
}
