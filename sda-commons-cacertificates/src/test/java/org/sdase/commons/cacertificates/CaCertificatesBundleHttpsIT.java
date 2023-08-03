/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.cacertificates;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;
import org.junit.jupiter.api.Test;
import org.sdase.commons.cacertificates.ssl.CertificateReader;
import org.sdase.commons.cacertificates.ssl.SslUtil;

class CaCertificatesBundleHttpsIT {
  private static final String DEFAULT_SSL_PROTOCOL = "TLSv1.2";

  private static final String securedHost = "https://google.com";

  @Test
  void shouldFailWithCustomTrustStore() throws Exception {

    CertificateReader certificateReader =
        new CertificateReader(Paths.get("src", "test", "resources").toString());
    Optional<String> pemContent = certificateReader.readCertificates();

    // create custom sslContext that has no trusted certificate
    SSLContext sslContext = createSSlContextWithoutDefaultMerging(pemContent.get());

    assertThatExceptionOfType(SSLHandshakeException.class)
        .isThrownBy(() -> callSecureEndpointWithSSLContextForStatus(sslContext));
  }

  @Test
  void shouldHttpsOK200WithDefaultTrustStore() throws Exception {
    // The default context should be created with an empty certificate bundle
    SSLContext sslContext = SslUtil.createSslContext(SslUtil.createTruststoreFromPemKey(null));
    assertThat(callSecureEndpointWithSSLContextForStatus(sslContext)).isEqualTo(200);
  }

  @Test
  void shouldMakeHttpsOK200withCustomTrustStore() throws Exception {

    var certificateReader = new CertificateReader(Paths.get("src", "test", "resources").toString());
    Optional<String> pemContent = certificateReader.readCertificates();

    // create custom sslContext that has no trusted certificate but falls back to JVM default
    SSLContext sslContext =
        SslUtil.createSslContext(SslUtil.createTruststoreFromPemKey(pemContent.get()));

    assertThat(callSecureEndpointWithSSLContextForStatus(sslContext)).isEqualTo(200);
  }

  public static int callSecureEndpointWithSSLContextForStatus(SSLContext sslContext)
      throws IOException {
    URLConnection urlConnection = new URL(securedHost).openConnection();
    if (urlConnection instanceof HttpsURLConnection httpsURLConnection) {
      httpsURLConnection.setSSLSocketFactory(sslContext.getSocketFactory());
      httpsURLConnection.connect();
      try (InputStream ignored = httpsURLConnection.getInputStream()) {
        return httpsURLConnection.getResponseCode();
      }
    }
    throw new IllegalStateException("Not a HttpsURLConnection");
  }

  private static SSLContext createSSlContextWithoutDefaultMerging(String pemContent)
      throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
    KeyStore truststore = SslUtil.createTruststoreFromPemKey(pemContent);
    SSLContext sslContext = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);

    String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
    TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(tmfAlgorithm);
    trustManagerFactory.init(truststore);

    sslContext.init(null, trustManagerFactory.getTrustManagers(), SslUtil.createSecureRandom());
    return sslContext;
  }
}
