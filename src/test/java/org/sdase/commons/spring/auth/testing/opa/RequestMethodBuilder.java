/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.auth.testing.opa;

public interface RequestMethodBuilder {
  RequestPathBuilder withHttpMethod(String httpMethod);
}
