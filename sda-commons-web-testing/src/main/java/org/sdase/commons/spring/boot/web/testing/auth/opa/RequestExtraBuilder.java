/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing.auth.opa;

public interface RequestExtraBuilder extends AllowBuilder {
  RequestExtraBuilder withJwt(String jwt);

  default RequestExtraBuilder withJwtFromHeaderValue(String jwtWithBearerPrefix) {
    if (jwtWithBearerPrefix == null || !jwtWithBearerPrefix.toLowerCase().startsWith("bearer ")) {
      return this;
    }
    return withJwt(jwtWithBearerPrefix.substring("bearer".length()).trim());
  }
}
