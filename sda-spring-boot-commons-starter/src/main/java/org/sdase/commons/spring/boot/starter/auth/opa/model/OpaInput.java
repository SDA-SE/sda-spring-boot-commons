/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.auth.opa.model;

public class OpaInput {

  /** trace token to be able to find opa debug */
  private final String trace;

  /** JWT received with the request */
  private final String jwt;

  /** url path to the resource without base url */
  private final String[] path;

  /** HTTP Method */
  private final String httpMethod;

  public OpaInput(String jwt, String[] path, String httpMethod, String traceToken) {
    this.jwt = jwt;
    this.path = path;
    this.httpMethod = httpMethod;
    this.trace = traceToken;
  }

  public String getJwt() {
    return jwt;
  }

  public String[] getPath() {
    return path;
  }

  public String getHttpMethod() {
    return httpMethod;
  }

  public String getTrace() {
    return trace;
  }
}
