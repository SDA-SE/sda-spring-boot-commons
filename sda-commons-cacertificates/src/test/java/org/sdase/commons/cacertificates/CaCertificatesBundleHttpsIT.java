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
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManagerFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
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
        .isThrownBy(() -> callSecureEndpointWithSSLContext(sslContext));
  }

  @Test
  void shouldHttpsOK200WithDefaultTrustStore() throws Exception {
    // The default context should be created with an empty certificate bundle
    SSLContext sslContext = SslUtil.createSslContext(SslUtil.createTruststoreFromPemKey(null));
    assertThat(callSecureEndpointWithSSLContext(sslContext).getStatusLine().getStatusCode())
        .isEqualTo(200);
  }

  @Test
  void shouldMakeHttpsOK200withCustomTrustStore() throws Exception {

    var certificateReader = new CertificateReader(Paths.get("src", "test", "resources").toString());
    Optional<String> pemContent = certificateReader.readCertificates();

    // create custom sslContext that has no trusted certificate but falls back to JVM default
    SSLContext sslContext =
        SslUtil.createSslContext(SslUtil.createTruststoreFromPemKey(pemContent.get()));

    assertThat(callSecureEndpointWithSSLContext(sslContext).getStatusLine().getStatusCode())
        .isEqualTo(200);
  }

  public static CloseableHttpResponse callSecureEndpointWithSSLContext(SSLContext sslContext)
      throws IOException {
    CloseableHttpClient httpclient = HttpClients.custom().setSSLContext(sslContext).build();
    return httpclient.execute(new HttpGet(securedHost));
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