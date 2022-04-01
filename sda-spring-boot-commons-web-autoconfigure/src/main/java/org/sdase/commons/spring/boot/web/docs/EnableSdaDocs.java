/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
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
