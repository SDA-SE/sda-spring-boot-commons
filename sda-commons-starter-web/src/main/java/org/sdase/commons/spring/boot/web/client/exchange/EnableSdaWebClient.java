package org.sdase.commons.spring.boot.web.client.exchange;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Import(SdaWebClientsRegistrar.class)
public @interface EnableSdaWebClient {

  /**
   * Base packages to scan for annotated components.
   * Allows for more concise annotation declarations e.g.:@ComponentScan("org.my.pkg") instead of @ComponentScan(basePackages="org.my.pkg")
   * @return the array of 'basePackages'.
   */
  String[] basePackages() default {};

}
