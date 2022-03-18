/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.ssl.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sdase.commons.spring.boot.starter.ssl.testing.TestHelper.extractCertificates;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.starter.security.ssl.SslContextConfigurator;

class SslContextConfiguratorTest {

  @AfterEach
  void tearDown() throws IOException {
    var trustStorePath =
        Optional.ofNullable(System.getProperty("javax.net.ssl.trustStore"))
            .map(Path::of)
            .filter(Files::exists);
    if (trustStorePath.isPresent()) {
      Files.delete(trustStorePath.get());
    }
  }

  @Test
  void shouldLoadFromConfiguredCertificatePath()
      throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
    // Given
    var testee = new SslContextConfigurator("src/test/resources/caCertificates/combined.pem");
    var beforeTrustStorePath = System.getProperty("javax.net.ssl.trustStore");

    // When
    testee.run(null);

    // Then
    var afterTrustStorePath = System.getProperty("javax.net.ssl.trustStore");
    final var ks = KeyStore.getInstance(new File(afterTrustStorePath), "changeit".toCharArray());
    var certificates = extractCertificates(ks);
    assertThat(beforeTrustStorePath).isNotEqualTo(afterTrustStorePath);
    assertThat(certificates).hasSize(3);
  }

  @Test
  void shouldNotLoadFromConfiguredCertificatePathIfNoFileCouldBeFound() {
    // Given
    var testee = new SslContextConfigurator("src/test/resources/caCertificates/unknown.pem");
    var beforeTrustStorePath = System.getProperty("javax.net.ssl.trustStore");

    // When
    testee.run(null);

    // Then
    var afterTrustStorePath = System.getProperty("javax.net.ssl.trustStore");
    assertThat(beforeTrustStorePath).isEqualTo(afterTrustStorePath);
  }
}
