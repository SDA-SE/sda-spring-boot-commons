/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.test;

import org.springframework.boot.test.context.SpringBootContextLoader;
import org.springframework.boot.test.context.SpringBootTestContextBootstrapper;
import org.springframework.boot.test.context.assertj.ApplicationContextAssert;
import org.springframework.boot.test.context.assertj.AssertableApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.cache.DefaultCacheAwareContextLoaderDelegate;
import org.springframework.test.context.support.DefaultBootstrapContext;

public class ContextUtils {
  private ContextUtils() {
    // avoid instantiation
  }

  /**
   * Creates a spring test context for testing application startup failures. Using {@link
   * org.springframework.boot.test.context.SpringBootTest} annotation will prevent the tests from
   * starting. Tests that need dependencies from the web environment must add
   * `@SpringBootTest(webEnvironment = ...)` to the tested Spring Boot application class because
   * that is how it is internally checked. The test class must not be annotated with {@link
   * org.springframework.boot.test.context.SpringBootTest}.
   *
   * <p>Example:
   *
   * <pre>
   *   <code>assertThat(createTestContext(AppFailingToInitialize.class)).hasFailed();</code>
   * </pre>
   *
   * @see ApplicationContextAssert for details about the assertions to verify conditions of an
   *     {@link org.springframework.context.ApplicationContext} that may have {@linkplain
   *     ApplicationContextAssert#hasFailed() failed to initialize}.
   * @param springBootApplicationClass The application class annotated with `SpringBootApplication`
   * @return An assertable application context.
   */
  public static AssertableApplicationContext createTestContext(
      Class<?> springBootApplicationClass) {
    var delegate = new DefaultCacheAwareContextLoaderDelegate();
    var bootstrapContext = new DefaultBootstrapContext(springBootApplicationClass, delegate);

    var bootstrapper = new SpringBootTestContextBootstrapper();
    bootstrapper.setBootstrapContext(bootstrapContext);

    var configuration = bootstrapper.buildMergedContextConfiguration();

    return AssertableApplicationContext.get(
        () -> {
          SpringBootContextLoader loader = new SpringBootContextLoader();
          try {
            return (ConfigurableApplicationContext) loader.loadContext(configuration);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        });
  }
}
