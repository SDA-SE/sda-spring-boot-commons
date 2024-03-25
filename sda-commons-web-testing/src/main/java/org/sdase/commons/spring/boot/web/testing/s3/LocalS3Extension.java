/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing.s3;

import com.robothy.s3.rest.LocalS3;
import com.robothy.s3.rest.bootstrap.LocalS3Mode;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.endpoints.S3EndpointProvider;

/**
 * @see S3Test
 */
public class LocalS3Extension
    implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, ParameterResolver {

  private static final Logger LOG = LoggerFactory.getLogger(LocalS3Extension.class);

  private static final String S3_CLIENT_CLASS_NAME = "software.amazon.awssdk.services.s3.S3Client";
  private static final String ROBOTHY_S3_SERVER_CLASS_NAME = "com.robothy.s3.rest.LocalS3";
  private static final String LOCAL_CONFIGURATION_CLASS_NAME =
      "org.sdase.commons.spring.boot.web.testing.s3.LocalS3Configuration";
  private static final String REGION = "local";

  private S3Test s3Test;
  private String accessKeyBefore;
  private String secretKeyBefore;
  private String endpointBefore;
  private String regionBefore;
  private String bucketNameBefore;
  private final List<AutoCloseable> closeOnFinish = new ArrayList<>();

  @Override
  public void beforeAll(ExtensionContext context) {
    verifyDependency();

    s3Test = context.getRequiredTestClass().getAnnotation(S3Test.class);
    S3Test.S3 properties = s3Test.configurationProperties();

    LocalS3 server = start();
    closeOnFinish.add(server::shutdown);

    accessKeyBefore = systemProperty(properties.accessKeyProperty(), UUID.randomUUID().toString());
    secretKeyBefore = systemProperty(properties.secretKeyProperty(), UUID.randomUUID().toString());
    endpointBefore =
        systemProperty(
            properties.endpointProperty(), "http://localhost:%d".formatted(server.getPort()));
    regionBefore = systemProperty(properties.regionProperty(), REGION);
    bucketNameBefore = systemProperty(properties.bucketNameProperty(), s3Test.bucketName());
    //noinspection resource
    createClient().createBucket(b -> b.bucket(s3Test.bucketName()));
  }

  @Override
  public void afterAll(ExtensionContext context) {
    verifyDependency();
    closeOnFinish.forEach(
        closeable -> {
          try {
            closeable.close();
          } catch (Exception e) {
            LOG.warn("Could not close {}", closeable, e);
          }
        });
    systemProperty(s3Test.configurationProperties().accessKeyProperty(), accessKeyBefore);
    systemProperty(s3Test.configurationProperties().secretKeyProperty(), secretKeyBefore);
    systemProperty(s3Test.configurationProperties().endpointProperty(), endpointBefore);
    systemProperty(s3Test.configurationProperties().regionProperty(), regionBefore);
    systemProperty(s3Test.configurationProperties().bucketNameProperty(), bucketNameBefore);
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    verifyDependency();
    if (s3Test.cleanBeforeEach()) {
      @SuppressWarnings("resource")
      S3Client client = createClient();
      client
          .listObjects(l -> l.bucket(s3Test.bucketName()))
          .contents()
          .forEach(o -> client.deleteObject(d -> d.bucket(s3Test.bucketName()).key(o.key())));
    }
  }

  private LocalS3 start() {
    try {
      LocalS3 localS3 = LocalS3.builder().port(-1).mode(LocalS3Mode.IN_MEMORY).build();
      localS3.start();
      return localS3;
    } catch (RuntimeException e) {
      throw new IllegalStateException("Failed to start Local S3.", e);
    }
  }

  @Override
  public boolean supportsParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    verifyDependency();
    String expectedClass = parameterContext.getParameter().getType().getName();
    return S3_CLIENT_CLASS_NAME.equals(expectedClass)
        || LOCAL_CONFIGURATION_CLASS_NAME.equals(expectedClass);
  }

  @Override
  public Object resolveParameter(
      ParameterContext parameterContext, ExtensionContext extensionContext)
      throws ParameterResolutionException {
    verifyDependency();
    String expectedClass = parameterContext.getParameter().getType().getName();
    return switch (expectedClass) {
      case S3_CLIENT_CLASS_NAME -> createClient();
      case LOCAL_CONFIGURATION_CLASS_NAME -> new LocalS3Configuration(
          System.getProperty(s3Test.configurationProperties().accessKeyProperty()),
          System.getProperty(s3Test.configurationProperties().secretKeyProperty()),
          System.getProperty(s3Test.configurationProperties().endpointProperty()),
          System.getProperty(s3Test.configurationProperties().regionProperty()),
          System.getProperty(s3Test.configurationProperties().bucketNameProperty()));
      default -> throw new IllegalArgumentException(
          "Can't resolve parameter of type %s".formatted(expectedClass));
    };
  }

  private S3Client createClient() {
    S3Client client =
        S3Client.builder()
            .forcePathStyle(true)
            .region(
                Region.of(System.getProperty(s3Test.configurationProperties().regionProperty())))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(
                        System.getProperty(s3Test.configurationProperties().accessKeyProperty()),
                        System.getProperty(s3Test.configurationProperties().secretKeyProperty()))))
            .endpointProvider(S3EndpointProvider.defaultProvider())
            .endpointOverride(
                URI.create(System.getProperty(s3Test.configurationProperties().endpointProperty())))
            .build();
    closeOnFinish.add(client);
    return client;
  }

  private String systemProperty(String propertyName, String propertyValue) {
    String previous = System.getProperty(propertyName);
    if (propertyValue == null) {
      System.clearProperty(propertyName);
    } else {
      System.setProperty(propertyName, propertyValue);
    }
    return previous;
  }

  private void verifyDependency() {
    try {
      getClass().getClassLoader().loadClass(ROBOTHY_S3_SERVER_CLASS_NAME);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(
          "Missing dependency. 'io.github.robothy:local-s3-rest' must be added to test dependencies.",
          e);
    }
    try {
      getClass().getClassLoader().loadClass(S3_CLIENT_CLASS_NAME);
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(
          "Missing dependency. 'software.amazon.awssdk:s3' must be available in test dependencies.",
          e);
    }
  }
}
