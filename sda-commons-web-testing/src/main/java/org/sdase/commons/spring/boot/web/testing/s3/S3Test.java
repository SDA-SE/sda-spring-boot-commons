/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing.s3;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Activates {@link LocalS3Extension}, starts a local S3 mock and provides {@link
 * software.amazon.awssdk.services.s3.S3Client} and {@link LocalS3Configuration} for injection in
 * test methods. For {@link org.springframework.boot.test.context.SpringBootTest}s, {@code S3Test}
 * must be defined before {@link org.springframework.boot.test.context.SpringBootTest} and sets
 * {@linkplain System#setProperty(String, String) system properties} as defined in {@link
 * S3Test#configurationProperties()} to be picked up by Spring Boot.
 *
 * <p>{@link LocalS3Configuration} and {@link software.amazon.awssdk.services.s3.S3Client} can be
 * used in test methods as well as {@link org.junit.jupiter.api.BeforeEach}, {@link
 * org.junit.jupiter.api.BeforeAll}, {@link org.junit.jupiter.api.AfterEach} and {@link
 * org.junit.jupiter.api.AfterAll} methods as parameter to get the current configuration for the
 * test and a client configured to access the local S3 mock.
 */
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(LocalS3Extension.class)
public @interface S3Test {
  @interface S3 {
    /**
     * @return property name of the access key used to connect to the local S3 mock
     */
    String accessKeyProperty() default "s3.accessKey";

    /**
     * @return property name of the secret key matching the {@linkplain #accessKeyProperty() access
     *     key} used to connect to the local S3 mock
     */
    String secretKeyProperty() default "s3.secretKey";

    /**
     * @return property name of the endpoint in the form {@code http://localhost:<port>}, note that
     *     {@link software.amazon.awssdk.services.s3.S3BaseClientBuilder#forcePathStyle(Boolean)}
     *     must be {@code true}, otherwise, the client will use the bucket name as subdomain which
     *     can't be resolved locally
     */
    String endpointProperty() default "s3.endpoint";

    /**
     * @return property name for the simulated AWS region of the local S3
     */
    String regionProperty() default "s3.region";

    /**
     * @return property name for the name of the bucket to be used in tests
     */
    String bucketNameProperty() default "s3.bucketName";
  }

  /**
   * @return keys of {@linkplain System#setProperty(String, String) system properties} that
   *     configure the applications S3 client
   */
  S3 configurationProperties() default @S3;

  /**
   * @return the name of the bucket to be used in tests
   */
  String bucketName() default "test-bucket";

  /**
   * @return if all objects in the S3 bucket should be removed in {@link
   *     org.junit.jupiter.api.BeforeEach}
   */
  boolean cleanBeforeEach() default true;
}
