/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.ssl;

import org.sdase.commons.spring.boot.starter.security.ssl.SslContextConfigurator;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SslContextConfigurator.class)
public class SecurityTestApp {}
