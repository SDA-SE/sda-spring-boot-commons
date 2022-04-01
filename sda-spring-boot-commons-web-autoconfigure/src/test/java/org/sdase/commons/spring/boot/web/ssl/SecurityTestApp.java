/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.ssl;

import org.sdase.commons.spring.boot.web.security.ssl.SslContextConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SslContextConfiguration.class)
public class SecurityTestApp {}
