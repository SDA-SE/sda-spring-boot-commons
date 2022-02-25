/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.auth.testing;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpHeaders;

/**
 * The {@code AuthBuilder} is used to build JWT authentication in test cases that is accepted by the
 * tested application if the test is configured with {@link EnableSdaAuthMockInitializer}. Properly
 * configured instances of the {@code AuthBuilder} can be created from the {@link AuthMock} using
 * {@link AuthMock#authentication()} within the test. {@link AuthMock} can be {@link
 * org.springframework.beans.factory.annotation.Autowired} in the test class.
 */
public class AuthBuilder {

  private final RsaKeyPair keyPair;

  private final String issuer;

  private String subject;

  private final Map<String, Object> claims = new HashMap<>();

  private String cachedToken;

  /**
   * Use {@link AuthMock#authentication()} to create {@code AuthBuilder} instances.
   *
   * @param keyPair the key that is used to sign the JWT
   * @param issuer the issuer to set in the JWT header
   */
  AuthBuilder(RsaKeyPair keyPair, String issuer) {
    this.keyPair = keyPair;
    this.issuer = issuer;
    this.cachedToken = null;
  }

  public AuthBuilder withSubject(String subject) {
    this.subject = subject;
    this.cachedToken = null;
    return this;
  }

  public AuthBuilder addClaim(String key, Boolean value) {
    this.claims.put(key, value);
    this.cachedToken = null;
    return this;
  }

  public AuthBuilder addClaim(String key, Integer value) {
    this.claims.put(key, value);
    this.cachedToken = null;
    return this;
  }

  public AuthBuilder addClaim(String key, Long value) {
    this.claims.put(key, value);
    this.cachedToken = null;
    return this;
  }

  public AuthBuilder addClaim(String key, String value) {
    this.claims.put(key, value);
    this.cachedToken = null;
    return this;
  }

  public AuthBuilder addClaim(String key, Integer[] value) {
    this.claims.put(key, value);
    this.cachedToken = null;
    return this;
  }

  public AuthBuilder addClaim(String key, Long[] value) {
    this.claims.put(key, value);
    this.cachedToken = null;
    return this;
  }

  public AuthBuilder addClaim(String key, String[] value) {
    this.claims.put(key, value);
    this.cachedToken = null;
    return this;
  }

  public AuthBuilder addClaim(String key, Double value) {
    this.claims.put(key, value);
    this.cachedToken = null;
    return this;
  }

  public AuthBuilder addClaim(String key, Date value) {
    this.claims.put(key, value);
    this.cachedToken = null;
    return this;
  }

  public AuthBuilder addClaims(Map<String, Object> claims) {
    if (!claims.values().stream().allMatch(this::isSupportedClaimType)) {
      throw new IllegalArgumentException("Claims contain invalid type: " + claims);
    }
    this.claims.putAll(claims);
    this.cachedToken = null;
    return this;
  }

  private boolean isSupportedClaimType(Object o) {
    return (o instanceof String)
        || (o instanceof String[])
        || (o instanceof Integer)
        || (o instanceof Integer[])
        || (o instanceof Long)
        || (o instanceof Long[])
        || (o instanceof Double)
        || (o instanceof Boolean)
        || (o instanceof Date);
  }

  /**
   * @return the signed and encoded token, e.g. {@code eyXXX.eyYYY.ZZZ}
   */
  public String token() {
    try {
      if (this.cachedToken == null) {
        var headerBuilder =
            new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(this.keyPair.getKeyId());
        var payloadBuilder =
            new JWTClaimsSet.Builder()
                .issuer(this.issuer)
                .jwtID(UUID.randomUUID().toString())
                .subject(this.subject);
        claims.keySet().forEach(key -> payloadBuilder.claim(key, claims.get(key)));
        var signedJwt = new SignedJWT(headerBuilder.build(), payloadBuilder.build());
        signedJwt.sign(new RSASSASigner(this.keyPair.getPrivateKey()));
        this.cachedToken = signedJwt.serialize();
      }
    } catch (JOSEException e) {
      throw new IllegalStateException("Failed to sign mock JWT", e);
    }
    return this.cachedToken;
  }

  /**
   * @return the signed and encoded token with {@code Bearer} prefix to be used directly as {@code
   *     Authorization} header value, e.g. {@code Bearer eyXXX.eyYYY.ZZZ}
   */
  public String headerValue() {
    return "Bearer " + token();
  }

  /**
   * @return a client that uses the signed token with {@code Bearer} prefix to authenticate in the
   *     {@value HttpHeaders#AUTHORIZATION} header.
   */
  public TestRestTemplate authenticatedClient() {
    return new TestRestTemplate(
        new RestTemplateBuilder().defaultHeader(HttpHeaders.AUTHORIZATION, headerValue()));
  }
}
