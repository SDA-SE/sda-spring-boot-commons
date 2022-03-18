/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.ssl.testing;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class TestHelper {

  private TestHelper() {}

  public static List<Certificate> extractCertificates(KeyStore truststore)
      throws KeyStoreException {
    Enumeration<String> aliases = truststore.aliases();
    List<Certificate> certificates = new ArrayList<>();
    while (aliases.hasMoreElements()) {
      certificates.add(truststore.getCertificate(aliases.nextElement()));
    }
    return certificates;
  }
}
