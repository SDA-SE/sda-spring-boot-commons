/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.async;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.sdase.commons.spring.boot.starter.auth.EnableSdaSecurity;
import org.sdase.commons.spring.boot.starter.auth.opa.Constraints;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configures Springs {@link org.springframework.scheduling.annotation.Async} executor to transfer
 * the request attributes of the current request to the {@link Thread} running the asynchronous
 * method.
 *
 * <p>{@link Constraints} from {@link EnableSdaSecurity} are available in the asynchronous {@link
 * Thread} as well.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@EnableAsync
@Import(SdaAsyncConfiguration.class)
public @interface EnableSdaAsyncWithRequestContext {}
