/*
 * Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
 *
 * All rights reserved.
 */
package org.sdase.commons.spring.boot.web;

import org.sdase.commons.spring.boot.web.error.ApiExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/org/sdase/commons/spring/boot/web/defaults.properties")
@Import(ApiExceptionHandler.class)
public class SdaSpringConfiguration {}
