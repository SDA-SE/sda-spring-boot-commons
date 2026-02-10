/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing.auth.opa;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.absent;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.matchingJsonPath;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import org.sdase.commons.spring.boot.web.testing.auth.opa.model.OpaMockResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

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

  private ResponseDefinitionBuilder getResponse(OpaMockResponse response) throws JacksonException {
    return aResponse()
        .withStatus(200)
        .withHeader("Content-type", "application/json")
        .withBody(om.writeValueAsBytes(response));
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

  MappingBuilder matchInput(String httpMethod, String[] paths) throws JacksonException {
    return matchAnyPostUrl()
        .withRequestBody(matchingJsonPath("$.input.httpMethod", equalTo(httpMethod)))
        .withRequestBody(
            matchingJsonPath("$.input.path", equalToJson(om.writeValueAsString(paths))));
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

    OpaMockResponse response = new OpaMockResponse().setResult(objectNode);
    wireMockServer.stubFor(mappingBuilder.willReturn(getResponse(response)));
  }
}
