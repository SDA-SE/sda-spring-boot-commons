/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.auth.testing.opa;

public interface RequestMethodBuilder {
  RequestPathBuilder withHttpMethod(String httpMethod);
}
