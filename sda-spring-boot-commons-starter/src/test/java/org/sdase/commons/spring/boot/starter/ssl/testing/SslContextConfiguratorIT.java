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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.starter.ssl.SecurityTestApp;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

@SpringBootTest(
    classes = SecurityTestApp.class,
    webEnvironment = WebEnvironment.RANDOM_PORT,
    properties =
        "ssl.trusted.combined.certificate.path:src/test/resources/caCertificates/combined.pem")
class SslContextConfiguratorIT {

  @AfterAll
  static void afterAll() throws IOException {
    var trustStorePath =
        Optional.ofNullable(System.getProperty("javax.net.ssl.trustStore")).map(Path::of);
    if (trustStorePath.isPresent()) {
      Files.delete(trustStorePath.get());
    }
  }

  @Test
  void shouldHaveConfiguredTrustStore()
      throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException {
    var afterTrustStorePath = System.getProperty("javax.net.ssl.trustStore");
    var ks = KeyStore.getInstance(new File(afterTrustStorePath), "changeit".toCharArray());
    var certs = extractCertificates(ks);
    assertThat(certs).hasSize(3);
  }
}
