/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.auth.testing.opa;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.sdase.commons.spring.boot.starter.auth.opa.model.OpaResponse;

public class StubBuilder
    implements RequestMethodBuilder, RequestPathBuilder, RequestExtraBuilder, AllowBuilder {

  private final WireMockServer wireMockServer;
  private final ObjectMapper om;
  private final boolean onAnyRequest;
  private String httpMethod;
  private String[] paths;
  private boolean matchJWT;
  private String jwt;
  private boolean allow;
  private Object constraint;

  public StubBuilder(WireMockServer wireMockServer, ObjectMapper om, boolean onAnyRequest) {
    this.wireMockServer = wireMockServer;
    this.om = om;
    this.onAnyRequest = onAnyRequest;
  }

  public boolean isAllow() {
    return allow;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String[] getPaths() {
    return paths;
  }

  public boolean isMatchJWT() {
    return matchJWT;
  }

  public String getJwt() {
    return jwt;
  }

  public boolean isOnAnyRequest() {
    return onAnyRequest;
  }

  public Object getConstraint() {
    return constraint;
  }

  private ResponseDefinitionBuilder getResponse(OpaResponse response) {
    try {
      return aResponse()
          .withStatus(200)
          .withHeader("Content-type", "application/json")
          .withBody(om.writeValueAsBytes(response));
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Mock initialization failed");
    }
  }

  public RequestPathBuilder withHttpMethod(String httpMethod) {
    this.httpMethod = httpMethod;
    return this;
  }

  public RequestExtraBuilder withPath(String path) {
    this.paths = splitPath(path);
    return this;
  }

  public RequestExtraBuilder withJwt(String jwt) {
    matchJWT = true;
    this.jwt = jwt;
    return this;
  }

  public void allow() {
    this.allow = true;
    this.build();
  }

  public void allowWithConstraint(Object constraint) {
    this.allow = true;
    this.constraint = constraint;
    this.build();
  }

  public void deny() {
    this.allow = false;
    this.build();
  }

  MappingBuilder matchAnyPostUrl() {
    return post(urlMatching("/v1/data/.*"));
  }

  MappingBuilder matchInput(String httpMethod, String[] paths) {
    try {
      return matchAnyPostUrl()
          .withRequestBody(matchingJsonPath("$.input.httpMethod", equalTo(httpMethod)))
          .withRequestBody(
              matchingJsonPath("$.input.path", equalToJson(om.writeValueAsString(paths))));
    } catch (JsonProcessingException e) {
      throw new IllegalStateException("Mock initialization failed", e);
    }
  }

  static String[] splitPath(String path) {
    if (path.startsWith("/")) {
      path = path.substring(1);
    }
    return path.split("/");
  }

  private void build() {

    MappingBuilder mappingBuilder;
    if (isOnAnyRequest()) {
      mappingBuilder = matchAnyPostUrl();
    } else {
      mappingBuilder = matchInput(getHttpMethod(), getPaths());

      if (isMatchJWT()) {
        mappingBuilder.withRequestBody(
            matchingJsonPath("$.input.jwt", getJwt() != null ? equalTo(getJwt()) : absent()));
      }
    }

    ObjectNode objectNode = om.createObjectNode();

    if (getConstraint() != null) {
      objectNode = om.valueToTree(getConstraint());
    }

    objectNode.put("allow", isAllow());

    OpaResponse response = new OpaResponse().setResult(objectNode);
    wireMockServer.stubFor(mappingBuilder.willReturn(getResponse(response)));
  }
}
