/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth.opa.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public record OpaInput(
    String jwt, String[] path, String httpMethod, String traceToken, JsonNode body) {
  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;

    OpaInput opaInput = (OpaInput) o;
    return Objects.equals(jwt, opaInput.jwt)
        && Arrays.equals(path, opaInput.path)
        && Objects.equals(body, opaInput.body)
        && Objects.equals(httpMethod, opaInput.httpMethod)
        && Objects.equals(traceToken, opaInput.traceToken);
  }

  @Override
  public int hashCode() {
    int result = Objects.hashCode(jwt);
    result = 31 * result + Arrays.hashCode(path);
    result = 31 * result + Objects.hashCode(httpMethod);
    result = 31 * result + Objects.hashCode(traceToken);
    result = 31 * result + Objects.hashCode(body);
    return result;
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
        .append("jwt", jwt)
        .append("path", path)
        .append("httpMethod", httpMethod)
        .append("traceToken", traceToken)
        .append("body", body)
        .toString();
  }
}
