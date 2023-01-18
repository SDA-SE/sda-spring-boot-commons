/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.security.headers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * Activates setting headers to the response that enhance the security of web applications. Usually
 * we do not provide web content from services. But we address the risks identified in the security
 * guide as:
 *
 * <ul>
 *   <li>"Risk: Cross Site Scripting (XSS)"
 *   <li>"Risk: Reloading content into Flash and PDFs"
 *   <li>"Risk: Clickjacking"
 *   <li>"Risk: Passing on visited URLs to third parties"
 *   <li>"Risk: Interpretation of content by the browser"
 * </ul>
 *
 * <p>This feature should only be enabled in services that provide frontend resources like HTML
 * pages themselves. Services that provide REST APIs only shall not activate this feature. If not
 * enabled, headers are set {@linkplain RestfulApiSecurityConfiguration according risks for backend
 * service}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({FrontendSecurityConfiguration.class})
public @interface EnableFrontendSecurity {}
