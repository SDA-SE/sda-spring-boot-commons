/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.cacertificates.ssl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CertificateReaderTest {
  // absolute path of resources directory
  private final String resourcesDirectory = Paths.get("src", "test", "resources").toString();

  @BeforeEach
  void clearKeyStore()
      throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null, null);

    Enumeration<String> aliases = ks.aliases();

    while (aliases.hasMoreElements()) {
      String alias = aliases.nextElement();

      ks.deleteEntry(alias);
    }
  }

  @Test
  void shouldReadPemContent() {
    CertificateReader certificateReader =
        new CertificateReader(resourcesDirectory.concat("/notEmpty"));

    Optional<String> actualCertificatesOptional = certificateReader.readCertificates();

    assertThat(actualCertificatesOptional)
        .isPresent()
        .hasValueSatisfying(
            content -> assertThat(content).contains(TestUtil.readPemContent("trusted.pem")));
  }

  @Test
  void shouldNotFailIfPathDoesNotExist() {
    String givenDirThatDoesNotExist = this.resourcesDirectory.concat("/does_not_exist");

    CertificateReader certificateReader = new CertificateReader(givenDirThatDoesNotExist);
    assertThatNoException().isThrownBy(certificateReader::readCertificates);
  }

  @Test
  void shouldOmitNonPemFiles() {

    CertificateReader certificateReader =
        new CertificateReader(resourcesDirectory.concat("/notEmpty"));

    Optional<String> actualCertificatesOptional = certificateReader.readCertificates();

    assertThat(actualCertificatesOptional)
        .isPresent()
        .hasValueSatisfying(
            content ->
                assertThat(content)
                    .doesNotContain(TestUtil.readPemContent("notEmpty/notPemFile.extension")));
  }

  @Test
  void readCustomCa() {
    CertificateReader certificateReader =
        new CertificateReader(resourcesDirectory.concat("/notEmpty"));

    Optional<String> actualCertificatesOptional = certificateReader.readCertificates();

    assertThat(actualCertificatesOptional)
        .isPresent()
        .hasValueSatisfying(
            content ->
                assertThat(content).contains(TestUtil.readPemContent("notEmpty/certificate.pem")));
  }
}
