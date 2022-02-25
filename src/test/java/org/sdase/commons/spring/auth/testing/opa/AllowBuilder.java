/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.auth.testing.opa;

public interface AllowBuilder {
  void allow();

  void allowWithConstraint(Object constraint);

  void deny();
}
