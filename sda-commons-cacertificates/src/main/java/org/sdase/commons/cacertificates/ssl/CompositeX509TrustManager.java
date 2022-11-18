/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.cacertificates.ssl;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Represent a list of {@link X509TrustManager}. If a certificate chain is trusted by any of the
 * composed TrustManagers, then it is trusted by CompositeX509TrustManager.<br>
 * Background : {@link javax.net.ssl.SSLContext#init(KeyManager[], TrustManager[], SecureRandom)}
 */
public class CompositeX509TrustManager implements X509TrustManager {
  private final List<X509TrustManager> trustManagerList;

  public CompositeX509TrustManager(List<X509TrustManager> trustManagerList) {
    this.trustManagerList = trustManagerList;
  }

  @Override
  public void checkClientTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    for (var trustManager : trustManagerList) {
      try {
        trustManager.checkClientTrusted(chain, authType);
        return;
      } catch (CertificateException e) {
        // nothing here
      }
    }

    // if no certificates are trusted
    throw new CertificateException("None of the trustManagers trust this certificate chain");
  }

  @Override
  public void checkServerTrusted(X509Certificate[] chain, String authType)
      throws CertificateException {
    for (var trustManager : trustManagerList) {
      try {
        trustManager.checkServerTrusted(chain, authType);
        return;
      } catch (CertificateException e) {
        // nothing here
      }
    }
    // if no certificates are trusted
    throw new CertificateException("None of the trustManagers trust this certificate chain");
  }

  @Override
  public X509Certificate[] getAcceptedIssuers() {
    return trustManagerList.stream()
        .map(X509TrustManager::getAcceptedIssuers)
        .flatMap(Arrays::stream)
        .toArray(X509Certificate[]::new);
  }
}
