/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.validation;

import javax.annotation.PostConstruct;
import org.sdase.commons.spring.boot.web.jackson.SdaObjectMapperConfiguration;
import org.sdase.commons.spring.boot.web.security.exception.InsecureConfigurationException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Component;

/**
 * Checks that custom error mappers are registered by the SdaObjectMapperConfiguration. The check is
 * indirectly performed by checking that the required bean is in the spring context. This class
 * checks for the risks identified in the security guide as:
 *
 * <ul>
 *   <li>Detection of confidential components
 * </ul>
 */
@Component
@ConditionalOnMissingBean(name = SdaObjectMapperConfiguration.OBJECT_MAPPER_BUILDER_BEAN_NAME)
public class CustomObjectMapperAdvice {

  private static final String JACKSON_CONFIGURATION_BEAN_CLASS = "Jackson2ObjectMapperBuilder";

  @PostConstruct
  public void check() {
    throw new InsecureConfigurationException(
        "Missing "
            + SdaObjectMapperConfiguration.OBJECT_MAPPER_BUILDER_BEAN_NAME
            + " bean from org.sdase.commons.spring.boot.web.jackson. The "
            + JACKSON_CONFIGURATION_BEAN_CLASS
            + " component registers custom mappers.");
  }
}
