/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.kafka;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;

/** TODO ADD DOCUMENTATION LATER */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Import({SdaKafkaConsumerConfiguration.class, SdaKafkaProducerConfiguration.class})
@EnableKafka
public @interface EnableSdaKafka {}
