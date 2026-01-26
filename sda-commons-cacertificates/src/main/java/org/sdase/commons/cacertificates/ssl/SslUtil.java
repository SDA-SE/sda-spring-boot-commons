/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.cacertificates.ssl;

import java.io.IOException;
import java.io.StringReader;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.X509TrustedCertificateBlock;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SslUtil {

  private static final Logger LOG = LoggerFactory.getLogger(SslUtil.class);

  private static final String DEFAULT_SSL_PROTOCOL = "TLSv1.2";

  private SslUtil() {}

  public static SSLContext createSslContext(@Nullable KeyStore keystore) {
    try {
      var trustManagers = new TrustManager[] {createCompositeTrustManager(keystore)};

      // create sslContext combining multi managers
      var sslContext = SSLContext.getInstance(DEFAULT_SSL_PROTOCOL);
      sslContext.init(null, trustManagers, createSecureRandom());
      return sslContext;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static KeyStore createTruststoreFromPemKey(@Nullable String certificateAsString) {
    if (certificateAsString == null) {
      return null;
    }
    try (var parser = new PEMParser(new StringReader(certificateAsString))) {
      var keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null, null);
      var i = 0;
      X509Certificate certificate;
      while ((certificate = parseCert(parser)) != null) {
        keyStore.setCertificateEntry("cert_" + i, certificate);
        i += 1;
      }
      return keyStore;
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  // only for testing
  public static X509Certificate parseCert(PEMParser parser)
      throws IOException, CertificateException {

    var certificateObject = parser.readObject();
    if (certificateObject == null) {
      return null;
    }
    if (certificateObject instanceof X509CertificateHolder certHolder) {
      return new JcaX509CertificateConverter().getCertificate(certHolder);
    }
    if (certificateObject instanceof X509TrustedCertificateBlock certHolder) {
      return new JcaX509CertificateConverter().getCertificate(certHolder.getCertificateHolder());
    }
    throw new CertificateException(
        "Could not read certificate of type " + certificateObject.getClass());
  }

  // only for testing
  public static SecureRandom createSecureRandom() throws NoSuchAlgorithmException {
    var algorithmNativePRNG = "NativePRNG";
    var algorithmWindowsPRNG = "Windows-PRNG";
    try {
      return SecureRandom.getInstance(algorithmNativePRNG);
    } catch (NoSuchAlgorithmException e) {
      LOG.warn(
          "Failed to create SecureRandom with algorithm {}. Falling back to {}."
              + "This should only happen on windows machines.",
          algorithmNativePRNG,
          algorithmWindowsPRNG,
          e);
      return SecureRandom.getInstance(algorithmWindowsPRNG);
    }
  }

  // only for testing
  public static X509TrustManager getTrustManager(String algorithm, KeyStore keyStore) {
    try {
      var factory = TrustManagerFactory.getInstance(algorithm);
      factory.init(keyStore);
      return Arrays.stream(factory.getTrustManagers())
          .filter(X509TrustManager.class::isInstance)
          .map(X509TrustManager.class::cast)
          .findFirst()
          .orElse(null);
    } catch (Exception e) {
      // nothing here
    }
    return null;
  }

  private static TrustManager createCompositeTrustManager(@Nullable KeyStore keystore) {
    String defaultTrustManagerAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
    var trustManagersList = new ArrayList<X509TrustManager>();
    trustManagersList.add(getTrustManager(defaultTrustManagerAlgorithm, null));

    // using a null key store will create another defaultTrustManager with same settings
    if (keystore != null) {
      // add a trust manager created from the given keystore
      trustManagersList.add(getTrustManager(defaultTrustManagerAlgorithm, keystore));
    }
    return new CompositeX509TrustManager(trustManagersList);
  }
}
