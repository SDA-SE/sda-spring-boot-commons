/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.cacertificates;

import javax.net.ssl.SSLContext;
import org.sdase.commons.cacertificates.ssl.CertificateReader;
import org.sdase.commons.cacertificates.ssl.SslUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
public class CaCertificatesConfiguration {
  private final CertificateReader certificateReader;

  public CaCertificatesConfiguration(
      @Value("${sda.caCertificates.certificatesDir:/var/trust/certificates}")
          String defaultTrustedCertificatesDir) {
    certificateReader = new CertificateReader(defaultTrustedCertificatesDir);
  }

  @Bean
  public SSLContext mongoSSlContext() {
    return certificateReader
        .readCertificates() // pem content as strings
        .map(SslUtil::createTruststoreFromPemKey) // a keystore instance that have certs loaded
        .map(SslUtil::createSslContext) // the sslContext created with the previous keystore
        .orElse(null);
  }
}
