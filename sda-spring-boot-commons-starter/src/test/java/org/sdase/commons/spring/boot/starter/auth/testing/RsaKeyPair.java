/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.starter.auth.testing;

import com.github.tomakehurst.wiremock.common.Json;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class RsaKeyPair {

  private static final Logger LOG = LoggerFactory.getLogger(RsaKeyPair.class);

  private final RSAKey rsaKey;
  private final RSAPrivateKey privateKey;

  RsaKeyPair() {
    try {
      var rsaKeyGenerator = new RSAKeyGenerator(2048);
      this.rsaKey =
          rsaKeyGenerator.keyID(UUID.randomUUID().toString()).keyUse(KeyUse.SIGNATURE).generate();
      this.privateKey = rsaKey.toRSAPrivateKey();
      LOG.info("Created key pair. JWK to verify: {}", Json.write(getPublicKeyForJwks()));
    } catch (Exception e) {
      throw new IllegalStateException("Failed to create a key pair to sign mock JWTs", e);
    }
  }

  String getKeyId() {
    return rsaKey.getKeyID();
  }

  RSAPrivateKey getPrivateKey() {
    return privateKey;
  }

  Map<String, Object> getPublicKeyForJwks() {
    return rsaKey.toPublicJWK().toJSONObject();
  }
}
