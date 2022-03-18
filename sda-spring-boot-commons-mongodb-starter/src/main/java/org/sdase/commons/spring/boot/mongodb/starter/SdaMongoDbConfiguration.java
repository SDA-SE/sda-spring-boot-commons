/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.mongodb.starter;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:/org/sdase/commons/spring/boot/mongodb/starter/defaults.properties")
public class SdaMongoDbConfiguration {}
