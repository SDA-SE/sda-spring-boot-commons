/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.sdase.commons.spring.boot.web.async.EnableSdaAsyncWithRequestContext;
import org.sdase.commons.spring.boot.web.auth.EnableSdaSecurity;
import org.sdase.commons.spring.boot.web.client.EnableSdaClients;
import org.sdase.commons.spring.boot.web.docs.EnableSdaDocs;
import org.sdase.commons.spring.boot.web.jackson.EnableSdaRestGuide;
import org.sdase.commons.spring.boot.web.monitoring.EnableSdaMonitoring;
import org.sdase.commons.spring.boot.web.security.EnableSdaWebSecurity;
import org.springframework.context.annotation.Import;

/**
 * Enables features that make a Spring Boot service compliant with the SDA SE Core Concepts.
 *
 * <p>So far this includes
 *
 * <ul>
 *   <li>{@linkplain EnableSdaRestGuide requirements of the RESTful API Guide}
 *   <li>{@linkplain EnableSdaDocs live documentation as OpenAPI 3}
 *   <li>{@linkplain EnableSdaSecurity requirements of authentication and authorization}
 *   <li>{@linkplain EnableSdaClients REST clients configured for the SDA Platform}
 *   <li>{@linkplain EnableSdaAsyncWithRequestContext keep the request context in async execution}
 * </ul>
 *
 * Additionally, the default context path is configured as {@code /api} to provide consistent
 * resource paths in the SDA Platform. If special cases require to change this, {@code
 * server.servlet.context-path} can be overridden in the {@code application.yaml}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@EnableSdaRestGuide
@EnableSdaWebSecurity
@EnableSdaDocs
@EnableSdaSecurity
@EnableSdaClients
@EnableSdaAsyncWithRequestContext
@EnableSdaMonitoring
@Import(SdaSpringConfiguration.class)
public @interface EnableSdaPlatform {}
