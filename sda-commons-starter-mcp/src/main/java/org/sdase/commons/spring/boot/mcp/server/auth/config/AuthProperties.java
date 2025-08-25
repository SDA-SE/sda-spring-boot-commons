/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth.config;

import java.util.HashSet;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.Delimiter;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth")
public class AuthProperties {
  private Boolean disable = Boolean.FALSE;

  @Delimiter(",")
  private Set<String> issuers = new HashSet<>();

  public Boolean getDisable() {
    return disable;
  }

  public AuthProperties setDisable(Boolean disable) {
    this.disable = disable;
    return this;
  }

  public Set<String> getIssuers() {
    return issuers;
  }

  public AuthProperties setIssuers(Set<String> issuers) {
    this.issuers = issuers;
    return this;
  }
}
