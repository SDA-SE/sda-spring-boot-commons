/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.sdase.commons.spring.boot.web.tracing.SdaTraceTokenClientConfiguration;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.annotation.AliasFor;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@FeignClient(
    configuration = {
      SdaTraceTokenClientConfiguration.class,
      OidcClientRequestConfiguration.class,
      AuthenticationPassThroughClientConfiguration.class
    })
public @interface PlatformClient {

  @AliasFor(annotation = FeignClient.class)
  String name() default "";

  @AliasFor(annotation = FeignClient.class)
  String url();
}
