/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.security.ssl;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.X509TrustedCertificateBlock;

public class SslUtil {

  private SslUtil() {}

  public static KeyStore createTruststoreFromFile(Path pathToCertificate) {
    try (PEMParser parser = new PEMParser(new FileReader(pathToCertificate.toFile()))) {
      KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
      keyStore.load(null, null);
      int i = 0;
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

  public static X509Certificate parseCert(PEMParser parser)
      throws IOException, CertificateException {

    Object certificateObject = parser.readObject();
    if (certificateObject == null) {
      return null;
    }
    if (certificateObject instanceof X509CertificateHolder) {
      X509CertificateHolder certHolder = (X509CertificateHolder) certificateObject;
      return new JcaX509CertificateConverter().getCertificate(certHolder);
    }
    if (certificateObject instanceof X509TrustedCertificateBlock) {
      X509CertificateHolder certHolder =
          ((X509TrustedCertificateBlock) certificateObject).getCertificateHolder();
      return new JcaX509CertificateConverter().getCertificate(certHolder);
    }
    throw new CertificateException(
        "Could not read certificate of type " + certificateObject.getClass());
  }
}
