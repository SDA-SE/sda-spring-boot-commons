/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.auth.testing.opa;

public interface RequestExtraBuilder extends AllowBuilder {
  RequestExtraBuilder withJwt(String jwt);

  default RequestExtraBuilder withJwtFromHeaderValue(String jwtWithBearerPrefix) {
    if (jwtWithBearerPrefix == null || !jwtWithBearerPrefix.toLowerCase().startsWith("bearer ")) {
      return this;
    }
    return withJwt(jwtWithBearerPrefix.substring("bearer".length()).trim());
  }
}
