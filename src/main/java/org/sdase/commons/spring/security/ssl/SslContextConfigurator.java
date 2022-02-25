/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.security.ssl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * This Component is responsible for looking CA certificate in PEM format in a default (but
 * configurable) directory and putting the parsed certificates into the truststore. These
 * certificates are used to verify SSL connections.
 *
 * <p>Runs on start up, if the {@linkplain SslContextConfigurator} is imported within a {@linkplain
 * org.springframework.boot.autoconfigure.SpringBootApplication},
 */
@Component
public class SslContextConfigurator implements ApplicationRunner {

  private static final Logger LOG = LoggerFactory.getLogger(SslContextConfigurator.class);

  public static final String DEFAULT_CA_PATH = "/var/trust/certificates/combined-ca.pem"; // NOSONAR
  private static final String KEY_STORE_TYPE = "JKS";
  private static final String KEY_STORE_FILE_PREFIX = "combined-cacerts";
  private static final String KEY_STORE_FILE_SUFFIX = ".jks";
  private static final String DEFAULT_KEY_STORE_PASSWORD = "changeit"; // NOSONAR
  private static final String SSL_TRUST_STORE = "javax.net.ssl.trustStore";
  private static final String SSL_TRUST_STORE_PASSWORD = "javax.net.ssl.trustStorePassword";
  private static final String SSL_TRUST_STORE_TYPE = "javax.net.ssl.trustStoreType";

  private final String trustedCertificatePath;

  public SslContextConfigurator(
      @Value("${ssl.trusted.combined.certificate.path:" + DEFAULT_CA_PATH + "}")
          String trustedCertificatePath) {
    this.trustedCertificatePath = trustedCertificatePath;
  }

  @Override
  public void run(ApplicationArguments args) {
    initializeSslContext();
  }

  private void initializeSslContext() {
    resolveCertificateFile(trustedCertificatePath)
        .ifPresentOrElse(
            this::configureTrustStore,
            () -> LOG.info("No trustedCertificate found in configured or in the default path."));
  }

  private void configureTrustStore(Path path) {
    try {
      LOG.info("Configuring ssl trust store for certificate in path {}", path);
      System.setProperty(SSL_TRUST_STORE, createKeyStoreFile(path).getPath());
      System.setProperty(SSL_TRUST_STORE_TYPE, KEY_STORE_TYPE);
      System.setProperty(SSL_TRUST_STORE_PASSWORD, DEFAULT_KEY_STORE_PASSWORD);
    } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
      LOG.error("Could not initialize ssl context!", e);
      throw new IllegalStateException("Could not configure ssl trust store!");
    }
  }

  private File createKeyStoreFile(Path pathToCertificate)
      throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
    File keyStoreFile =
        File.createTempFile(KEY_STORE_FILE_PREFIX, KEY_STORE_FILE_SUFFIX); // NOSONAR
    try (FileOutputStream fos = new FileOutputStream(keyStoreFile.getPath())) {
      var ks = SslUtil.createTruststoreFromFile(pathToCertificate);
      ks.store(fos, DEFAULT_KEY_STORE_PASSWORD.toCharArray());
    }
    return keyStoreFile;
  }

  private Optional<Path> resolveCertificateFile(String pathToCertificate) {
    return toPathIfExists(pathToCertificate).filter(this::isPemFile);
  }

  private Optional<Path> toPathIfExists(String location) {

    Optional<Path> path = Optional.of(Paths.get(location)).filter(Files::exists);
    if (path.isPresent() && !Files.isReadable(path.get())) {
      throw new IllegalStateException(String.format("Existing file %s is not readable.", location));
    }
    return path;
  }

  private boolean isPemFile(Path filePath) {
    boolean isPemFile = filePath.getFileName().toString().endsWith(".pem");
    if (!isPemFile) {
      LOG.info("Omitting {}: not a .pem file", filePath);
    }
    return isPemFile;
  }
}
