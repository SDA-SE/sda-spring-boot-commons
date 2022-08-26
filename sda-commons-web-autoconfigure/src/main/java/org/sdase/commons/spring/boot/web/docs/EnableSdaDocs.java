/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.docs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * Enables a customized version of OpenAPI3 docs that is served at {@code /openapi.json} and {@code
 * /openapi.yaml}.
 *
 * <p>So far this covers:
 *
 * <ul>
 *   <li>The `servers` tag is removed from the OpenAPI.
 *   <li>The OpenApi generation is deterministic.
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({SdaOpenApiCustomizerConfiguration.class})
public @interface EnableSdaDocs {}
