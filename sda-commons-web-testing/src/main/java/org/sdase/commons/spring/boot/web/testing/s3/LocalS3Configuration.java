/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing.s3;

/**
 * Provides all configuration information for a local S3 mock started via {@link S3Test} in tests.
 * {@code LocalS3Configuration} can be used in test methods as well as {@link
 * org.junit.jupiter.api.BeforeEach}, {@link org.junit.jupiter.api.BeforeAll}, {@link
 * org.junit.jupiter.api.AfterEach} and {@link org.junit.jupiter.api.AfterAll} methods as parameter
 * to get the current configuration for the test.
 *
 * @param accessKey the access key for the S3 connection, effectively any access key would work with
 *     the S3 mock
 * @param secretKey the secret key matching the {@code accessKey} for the S3 connection, effectively
 *     any secret key would work with the S3 mock
 * @param endpoint the endpoint in the form {@code http://localhost:<port>}, note that {@link
 *     software.amazon.awssdk.services.s3.S3BaseClientBuilder#forcePathStyle(Boolean)} must be
 *     {@code true}, otherwise, the client will use the bucket name as subdomain which can't be
 *     resolved locally
 * @param region the simulated AWS region of the local S3
 * @param bucketName the {@linkplain S3Test#bucketName() configured bucket name}
 */
public record LocalS3Configuration(
    String accessKey, String secretKey, String endpoint, String region, String bucketName) {}
