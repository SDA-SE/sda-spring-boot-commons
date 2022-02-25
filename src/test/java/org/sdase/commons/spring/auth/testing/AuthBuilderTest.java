/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.auth.testing;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class AuthBuilderTest {

  private final AuthBuilder authBuilder =
      new AuthMock(
              Mockito.mock(WireMockServer.class, Mockito.RETURNS_DEEP_STUBS), new ObjectMapper())
          .authentication();

  @Test
  void shouldAddIntegerClaim() throws ParseException {
    String token = authBuilder.addClaim("testKey", 42).token();
    Integer claim = decodeJwt(token).getJWTClaimsSet().getIntegerClaim("testKey");
    assertThat(claim).isEqualTo(42);
  }

  @Test
  void shouldAddLongClaim() throws ParseException {
    String token = authBuilder.addClaim("testKey", 2L + Integer.MAX_VALUE).token();
    Long claim = decodeJwt(token).getJWTClaimsSet().getLongClaim("testKey");
    assertThat(claim).isEqualTo(2147483649L);
  }

  @Test
  void shouldAddStringClaim() throws ParseException {
    String token = authBuilder.addClaim("testKey", "hello").token();
    String claim = decodeJwt(token).getJWTClaimsSet().getStringClaim("testKey");
    assertThat(claim).isEqualTo("hello");
  }

  @Test
  void shouldAddBooleanClaim() throws ParseException {
    String token = authBuilder.addClaim("testKey", true).token();
    Boolean claim = decodeJwt(token).getJWTClaimsSet().getBooleanClaim("testKey");
    assertThat(claim).isTrue();
  }

  @Test
  void shouldAddDoubleClaim() throws ParseException {
    String token = authBuilder.addClaim("testKey", 3.141D).token();
    Double claim = decodeJwt(token).getJWTClaimsSet().getDoubleClaim("testKey");
    assertThat(claim).isEqualTo(3.141D);
  }

  @Test
  void shouldAddDateClaim() throws ParseException {
    Date testDate = new Date();
    String token = authBuilder.addClaim("testKey", testDate).token();
    JWT claim = decodeJwt(token);
    assertThat(claim.getJWTClaimsSet().getDateClaim("testKey")).isEqualToIgnoringMillis(testDate);
  }

  @Test
  void shouldAddStringArrayClaim() throws ParseException {
    String token = authBuilder.addClaim("testKey", new String[] {"Hello", "World"}).token();
    JWT claim = decodeJwt(token);
    assertThat(claim.getJWTClaimsSet().getClaim("testKey"))
        .asList()
        .containsExactly("Hello", "World");
  }

  @Test
  void shouldAddLongArrayClaim() throws ParseException {
    String token = authBuilder.addClaim("testKey", new Long[] {1L, 2L}).token();
    JWT claim = decodeJwt(token);
    assertThat(claim.getJWTClaimsSet().getClaim("testKey")).asList().containsExactly(1L, 2L);
  }

  @Test
  void shouldAddIntArrayClaim() throws ParseException {
    String token = authBuilder.addClaim("testKey", new Integer[] {1, 2}).token();
    JWT claim = decodeJwt(token);
    assertThat(claim.getJWTClaimsSet().getClaim("testKey")).asList().containsExactly(1L, 2L);
  }

  @Test
  void shouldAddAllSupportedTypesWithOneCall() throws ParseException {
    Date dateValue = new Date();
    Map<String, Object> claims = new HashMap<>();
    claims.put("s", "Hello");
    claims.put("i", 42);
    claims.put("l", 2L + Integer.MAX_VALUE);
    claims.put("d", 3.141D);
    claims.put("b", true);
    claims.put("s[]", new String[] {"Hello", "World"});
    claims.put("i[]", new Integer[] {1, 2});
    claims.put("l[]", new Long[] {1L, 2L});
    claims.put("date", dateValue);
    String token = authBuilder.addClaims(claims).token();
    JWT jwt = decodeJwt(token);
    assertThat(jwt.getJWTClaimsSet().getStringClaim("s")).isEqualTo("Hello");
    assertThat(jwt.getJWTClaimsSet().getIntegerClaim("i")).isEqualTo(42);
    assertThat(jwt.getJWTClaimsSet().getLongClaim("l")).isEqualTo(2147483649L);
    assertThat(jwt.getJWTClaimsSet().getDoubleClaim("d")).isEqualTo(3.141D);
    assertThat(jwt.getJWTClaimsSet().getBooleanClaim("b")).isTrue();
    assertThat(jwt.getJWTClaimsSet().getStringListClaim("s[]")).containsExactly("Hello", "World");
    assertThat(jwt.getJWTClaimsSet().getClaim("i[]")).asList().containsExactly(1L, 2L);
    assertThat(jwt.getJWTClaimsSet().getClaim("l[]")).asList().containsExactly(1L, 2L);
    assertThat(jwt.getJWTClaimsSet().getDateClaim("date")).isEqualToIgnoringMillis(dateValue);
  }

  @Test
  void shouldFailForAnyClaimOfInvalidType() {
    Date dateValue = new Date();
    Map<String, Object> claims = new LinkedHashMap<>();
    claims.put("s", "Hello");
    claims.put("i", 42);
    claims.put("l", 2L + Integer.MAX_VALUE);
    claims.put("d", 3.141D);
    claims.put("b", true);
    claims.put("s[]", new String[] {"Hello", "World"});
    claims.put("i[]", new Integer[] {1, 2});
    claims.put("l[]", new Long[] {1L, 2L});
    claims.put("date", dateValue);
    claims.put("invalid", Instant.now());
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> this.authBuilder.addClaims(claims));
  }

  @Test
  void shouldOverwriteClaimOnMultipleAddCalls() throws ParseException {
    String token =
        authBuilder.addClaim("test", 1L).addClaim("test", 2).addClaim("test", "foo").token();
    JWT jwt = decodeJwt(token);
    var jwtClaimsSet = jwt.getJWTClaimsSet();
    assertThatExceptionOfType(ParseException.class)
        .isThrownBy(() -> jwtClaimsSet.getLongClaim("test"));
    assertThatExceptionOfType(ParseException.class)
        .isThrownBy(() -> jwtClaimsSet.getIntegerClaim("test"));
    assertThat(jwtClaimsSet.getStringClaim("test")).isEqualTo("foo");
  }

  private JWT decodeJwt(String token) throws ParseException {
    return JWTParser.parse(token);
  }
}
