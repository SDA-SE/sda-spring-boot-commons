/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;

public class AuthHeaderClientInterceptor implements RequestInterceptor {

  private static final Logger LOG = LoggerFactory.getLogger(AuthHeaderClientInterceptor.class);

  @Override
  public void apply(RequestTemplate template) {
    firstAuthHeaderFromServletRequest()
        .ifPresent(authHeader -> template.header(HttpHeaders.AUTHORIZATION, authHeader));
  }

  Optional<String> firstAuthHeaderFromServletRequest() {
    try {
      var attribute =
          RequestContextHolder.currentRequestAttributes()
              .getAttribute(
                  AuthorizationStoreRequestInterceptor.ATTRIBUTE_NAME,
                  AuthorizationStoreRequestInterceptor.SCOPE);
      if (attribute instanceof String strAttribute) {
        return Optional.of(strAttribute);
      } else {
        return Optional.empty();
      }
    } catch (Exception e) {
      LOG.info("No authentication header: Not in a request context.");
      return Optional.empty();
    }
  }
}
