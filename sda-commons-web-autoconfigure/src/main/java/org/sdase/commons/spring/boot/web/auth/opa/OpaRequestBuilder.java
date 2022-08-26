/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.opa;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import org.sdase.commons.spring.boot.web.auth.opa.extension.OpaInputExtension;
import org.sdase.commons.spring.boot.web.auth.opa.model.OpaInput;
import org.sdase.commons.spring.boot.web.auth.opa.model.OpaRequest;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class OpaRequestBuilder {

  /** The header name used to send the request token. */
  // TODO This needs to be a global constant in a place that is more related to the trace token.
  private static final String TOKEN_HEADER = "Trace-Token";

  private final ObjectMapper objectMapper;
  private final List<OpaInputExtension<?>> opaInputExtensions;

  public OpaRequestBuilder(
      ObjectMapper objectMapper, List<OpaInputExtension<?>> opaInputExtensions) {
    this.objectMapper = objectMapper;
    this.opaInputExtensions = opaInputExtensions;
  }

  OpaRequest buildRequestPayload(HttpServletRequest request) {
    OpaInput opaInput = createBasicOpaInput(request);
    ObjectNode opaInputJson = finalizeOpaInputWithExtensions(request, opaInput);
    return OpaRequest.request(opaInputJson);
  }

  private OpaInput createBasicOpaInput(HttpServletRequest request) {
    String jwt = extractJwtIfAuthenticated(request);
    String[] path = extractPathSegments(request);
    String httpMethod = request.getMethod();
    String traceToken = request.getHeader(TOKEN_HEADER);
    return new OpaInput(jwt, path, httpMethod, traceToken);
  }

  private ObjectNode finalizeOpaInputWithExtensions(HttpServletRequest request, OpaInput opaInput) {
    var opaInputJson = objectMapper.convertValue(opaInput, ObjectNode.class);
    opaInputExtensions.forEach(
        extension ->
            opaInputJson.set(
                extension.getNamespace(),
                objectMapper.valueToTree(extension.createAdditionalInputContent(request))));
    return opaInputJson;
  }

  private String[] extractPathSegments(HttpServletRequest request) {
    var path = request.getServletPath();
    if (path == null) {
      return new String[] {};
    }
    return Stream.of(path.split("/"))
        .filter(Objects::nonNull)
        .filter(s -> !s.isEmpty())
        .toArray(String[]::new);
  }

  private String extractJwtIfAuthenticated(HttpServletRequest request) {
    var principal = request.getUserPrincipal();
    if (principal instanceof JwtAuthenticationToken jwtAuthenticationToken) {
      return jwtAuthenticationToken.getToken().getTokenValue();
    }
    return null;
  }
}
