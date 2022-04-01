/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.web.ssl.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.sdase.commons.spring.boot.web.ssl.testing.TestHelper.extractCertificates;

import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.web.security.ssl.SslUtil;

class SslUtilTest {

  @Test
  void shouldReadCombinedCa() throws KeyStoreException {
    KeyStore truststore =
        SslUtil.createTruststoreFromFile(Path.of("src/test/resources/caCertificates/combined.pem"));

    assertThat(truststore).isNotNull();
    List<Certificate> certificates = extractCertificates(truststore);
    assertThat(certificates).hasSize(3);
  }
}
