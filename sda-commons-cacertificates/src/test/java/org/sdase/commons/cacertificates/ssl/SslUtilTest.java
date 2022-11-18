/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.cacertificates.ssl;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.junit.jupiter.api.Test;

class SslUtilTest {

  private static final String DEFAULT_SSL_PROTOCOL = "TLSv1.2";
  // absolute path of resources directory
  private final String resourcesDirectory = Paths.get("src", "test", "resources").toString();

  @Test
  void shouldReadCombinedCa() throws KeyStoreException {
    String pemContent = TestUtil.readPemContent("combined.pem");

    KeyStore truststore = SslUtil.createTruststoreFromPemKey(pemContent);

    assertThat(truststore).isNotNull();
    List<Certificate> certificates = extractCertificates(truststore);
    assertThat(certificates).hasSize(3);
  }

  @Test
  void shouldFindAllCertificatesRecursively() throws KeyStoreException {
    CertificateReader certificateReader = new CertificateReader(resourcesDirectory);

    String pemContent = certificateReader.readCertificates().get();

    KeyStore truststore = SslUtil.createTruststoreFromPemKey(pemContent);

    List<Certificate> certificates = extractCertificates(truststore);
    assertThat(truststore).isNotNull();
    assertThat(certificates).hasSize(6);
  }

  @Test
  void shouldCreateSslContext() {
    KeyStore givenTrustStore =
        SslUtil.createTruststoreFromPemKey(TestUtil.readPemContent("trusted.pem"));

    SSLContext sslContext = SslUtil.createSslContext(givenTrustStore);

    assertThat(sslContext)
        .isNotNull()
        .extracting(SSLContext::getProtocol)
        .isEqualTo(DEFAULT_SSL_PROTOCOL);
  }

  private List<Certificate> extractCertificates(KeyStore truststore) throws KeyStoreException {
    Enumeration<String> aliases = truststore.aliases();
    List<Certificate> certificates = new ArrayList<>();
    while (aliases.hasMoreElements()) {
      certificates.add(truststore.getCertificate(aliases.nextElement()));
    }
    return certificates;
  }
}
