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
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
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
  @ConditionalOnMissingBean
  public SSLContext sslContext() {
    var certificatesAsString =
        certificateReader.readCertificates().orElse(null); // pem content as strings
    var keyStoreOrNull = SslUtil.createTruststoreFromPemKey(certificatesAsString);
    return SslUtil.createSslContext(keyStoreOrNull);
  }
}
