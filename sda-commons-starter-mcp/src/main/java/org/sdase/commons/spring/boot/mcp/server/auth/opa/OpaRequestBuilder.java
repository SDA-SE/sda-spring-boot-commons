/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.mcp.server.auth.opa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Stream;
import org.sdase.commons.spring.boot.mcp.server.auth.opa.extension.OpaInputExtension;
import org.sdase.commons.spring.boot.mcp.server.auth.opa.model.OpaInput;
import org.sdase.commons.spring.boot.mcp.server.auth.opa.model.OpaRequest;
import org.sdase.commons.spring.boot.mcp.server.filter.MultipleReadHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
class OpaRequestBuilder {

  private static final String TRACE_TOKEN_HEADER_NAME = "Trace-Token";
  private static final Logger LOGGER = LoggerFactory.getLogger(OpaRequestBuilder.class);

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
    return new OpaRequest(opaInputJson);
  }

  private OpaInput createBasicOpaInput(HttpServletRequest request) {
    String jwt = extractJwtIfAuthenticated(request);
    String[] path = extractPathSegments(request);
    String httpMethod = request.getMethod();
    String traceToken = request.getHeader(TRACE_TOKEN_HEADER_NAME);
    JsonNode body = extractRequestBody(request);
    return new OpaInput(jwt, path, httpMethod, traceToken, body);
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
    return Stream.of(path.split("/")).filter(s -> !s.isEmpty()).toArray(String[]::new);
  }

  private String extractJwtIfAuthenticated(HttpServletRequest request) {
    var principal = request.getUserPrincipal();
    if (principal instanceof JwtAuthenticationToken jwtAuthenticationToken) {
      return jwtAuthenticationToken.getToken().getTokenValue();
    }
    return null;
  }

  private JsonNode extractRequestBody(HttpServletRequest request) {
    HttpServletRequest unwrapped = unwrapToCachingRequest(request);
    try (InputStream is = unwrapped.getInputStream()) {
      String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
      if (body.isBlank()) {
        return null;
      }
      return getJsonNode(body);
    } catch (IOException e) {
      LOGGER.error("Error reading request body: {}", e.getMessage());
      return null;
    }
  }

  private JsonNode getJsonNode(String body) {
    try {
      return objectMapper.readTree(body);
    } catch (IOException e) {
      LOGGER.warn("Request body is not valid JSON: {}", e.getMessage());
      return null;
    }
  }

  private HttpServletRequest unwrapToCachingRequest(HttpServletRequest request) {
    HttpServletRequest current = request;
    while (current instanceof HttpServletRequestWrapper wrapper) {
      if (current instanceof MultipleReadHttpRequest) {
        return current;
      }
      current = (HttpServletRequest) wrapper.getRequest();
    }
    return request; // fallback, if no caching wrapper found
  }
}
