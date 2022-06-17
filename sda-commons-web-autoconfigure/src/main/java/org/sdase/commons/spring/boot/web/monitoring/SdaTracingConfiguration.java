/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.monitoring;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:/org/sdase/commons/spring/boot/web/monitoring/tracing.properties")
@AutoConfiguration
public class SdaTracingConfiguration {}
