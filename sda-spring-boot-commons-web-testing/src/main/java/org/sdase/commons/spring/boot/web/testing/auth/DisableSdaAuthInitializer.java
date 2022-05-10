/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.testing.auth;

import java.util.Map;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;

/**
 * To be used on test classes with {@link org.springframework.boot.test.context.SpringBootTest} as
 * {@linkplain ContextConfiguration#initializers() initializer} in a {@link ContextConfiguration} to
 * disable all authentication verification. Authorization can be provided with a {@literal MockBean}
 * of the custom constraints model.
 *
 * <p>Example
 *
 * <pre>
 *   <code>{@literal @}SpringBootTest
 *    {@literal @}ContextConfiguration(initializers = DisableSdaAuthInitializer.class)
 *    class MyTest {
 *      {@literal @}MockBean private MyConstraints myConstraints;
 *
 *    }</code>
 * </pre>
 */
@Configuration
public class DisableSdaAuthInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext> {
  @Override
  public void initialize(ConfigurableApplicationContext applicationContext) {
    TestPropertyValues.of(Map.of("auth.disable", "true", "opa.disable", "true"))
        .applyTo(applicationContext);
  }
}
