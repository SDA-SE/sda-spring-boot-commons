/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.internals.BrokerSecurityConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestApp;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestModel;
import org.sdase.commons.spring.boot.kafka.test.KafkaTestProducer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;

@SpringBootTest(
    classes = KafkaTestApp.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK,
    properties = {
      "management.server.port=0",
      "spring.kafka.properties.security.protocol=SASL_PLAINTEXT",
      "spring.kafka.properties.sasl.mechanism=OAUTHBEARER",
      "spring.kafka.properties.sasl.jaas.config=org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required clientId=\"oidcClient\" clientSecret=\"s3cret\";",
      "spring.kafka.properties.sasl.login.callback.handler.class=org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginCallbackHandler",
      "spring.kafka.properties.sasl.oauthbearer.token.endpoint.url="
          + KafkaOauthbearerIntegrationTest.TOKEN_ENDPOINT_URL
    })
@EmbeddedKafka(
    partitions = 1,
    brokerProperties = {
      "listeners=EXTERNAL://127.0.0.1:0,CONTROLLER://127.0.0.1:0",
      "listener.security.protocol.map=CONTROLLER:PLAINTEXT,EXTERNAL:SASL_PLAINTEXT",
      "controller.listener.names=CONTROLLER",
      "inter.broker.listener.name=EXTERNAL",
      "sasl.mechanism.inter.broker.protocol=OAUTHBEARER",
      "sasl.enabled.mechanisms=OAUTHBEARER",
      "listener.name.external.oauthbearer.sasl.jaas.config=org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required clientId=\"oidcClient\" clientSecret=\"s3cret\";",
      "listener.name.external.oauthbearer.sasl.login.callback.handler.class=org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginCallbackHandler",
      "listener.name.external.oauthbearer.sasl.server.callback.handler.class=org.apache.kafka.common.security.oauthbearer.OAuthBearerValidatorCallbackHandler",
      "listener.name.external.oauthbearer.sasl.oauthbearer.token.endpoint.url="
          + KafkaOauthbearerIntegrationTest.TOKEN_ENDPOINT_URL,
      "listener.name.external.oauthbearer.sasl.oauthbearer.jwks.endpoint.url="
          + KafkaOauthbearerIntegrationTest.JWKS_ENDPOINT_URL,
      "listener.name.external.oauthbearer.sasl.oauthbearer.expected.issuer="
          + KafkaOauthbearerIntegrationTest.ISSUER
    })
class KafkaOauthbearerIntegrationTest {

  private static final WireMockServer WIRE_MOCK_SERVER =
      new WireMockServer(new WireMockConfiguration().dynamicPort());

  static final String ISSUER = "http://localhost:${wiremock.auth.port}/issuer";
  static final String TOKEN_ENDPOINT_URL = ISSUER + "/token";
  static final String JWKS_ENDPOINT_URL = ISSUER + "/keys";
  private static final String ALLOWED_URLS_PROPERTY =
      BrokerSecurityConfigs.ALLOWED_SASL_OAUTHBEARER_URLS_CONFIG;
  private static final String CLIENT_ID = "oidcClient";
  private static final String CLIENT_SECRET = "s3cret";
  private static final RSAKey RSA_KEY = createRsaKey();

  private static String previousAllowedUrls;
  private static String accessToken;

  @Autowired EmbeddedKafkaBroker kafkaEmbedded;

  @Autowired KafkaTestProducer kafkaTestProducer;

  @Value("${app.kafka.producer.topic}")
  String topic;

  @BeforeAll
  static void beforeAll() {
    WIRE_MOCK_SERVER.start();
    accessToken = createAccessToken();
    stubOidcEndpoints();

    previousAllowedUrls = System.getProperty(ALLOWED_URLS_PROPERTY);
    System.setProperty("wiremock.auth.port", String.valueOf(WIRE_MOCK_SERVER.port()));
    System.setProperty(
        ALLOWED_URLS_PROPERTY,
        String.join(
            ",",
            WIRE_MOCK_SERVER.baseUrl() + "/issuer/token",
            WIRE_MOCK_SERVER.baseUrl() + "/issuer/keys"));
  }

  @AfterAll
  static void afterAll() {
    try {
      WIRE_MOCK_SERVER.stop();
    } finally {
      System.clearProperty("wiremock.auth.port");
      if (previousAllowedUrls == null) {
        System.clearProperty(ALLOWED_URLS_PROPERTY);
      } else {
        System.setProperty(ALLOWED_URLS_PROPERTY, previousAllowedUrls);
      }
    }
  }

  @Test
  void shouldProduceMessageWithOauthbearer()
      throws ExecutionException, InterruptedException, TimeoutException {
    KafkaTestModel given = new KafkaTestModel().setCheckInt(1).setCheckString("Hello World!");

    kafkaTestProducer.send(given);

    var actualRecords = consumeRecords(topic);
    assertThat(actualRecords)
        .hasSize(1)
        .first()
        .extracting(ConsumerRecord::value)
        .asString()
        .contains("Hello World!");

    WIRE_MOCK_SERVER.verify(getRequestedFor(urlEqualTo("/issuer/keys")));
  }

  ConsumerRecords<String, String> consumeRecords(String topic) {
    try (var testConsumer = createOauthbearerConsumer(topic)) {
      return testConsumer.poll(Duration.ofSeconds(10));
    }
  }

  KafkaConsumer<String, String> createOauthbearerConsumer(String topic) {
    Map<String, Object> consumerProps =
        new HashMap<>(KafkaTestUtils.consumerProps(kafkaEmbedded, "test-consumer", true));
    consumerProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
    consumerProps.put(SaslConfigs.SASL_MECHANISM, "OAUTHBEARER");
    consumerProps.put(
        SaslConfigs.SASL_JAAS_CONFIG,
        "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required "
            + "clientId=\""
            + CLIENT_ID
            + "\" clientSecret=\""
            + CLIENT_SECRET
            + "\";");
    consumerProps.put(
        SaslConfigs.SASL_LOGIN_CALLBACK_HANDLER_CLASS,
        "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginCallbackHandler");
    consumerProps.put(
        SaslConfigs.SASL_OAUTHBEARER_TOKEN_ENDPOINT_URL,
        WIRE_MOCK_SERVER.baseUrl() + "/issuer/token");

    KafkaConsumer<String, String> consumer =
        new KafkaConsumer<>(consumerProps, new StringDeserializer(), new StringDeserializer());
    consumer.subscribe(Set.of(topic));
    return consumer;
  }

  private static void stubOidcEndpoints() {
    WIRE_MOCK_SERVER.stubFor(
        post(urlEqualTo("/issuer/token"))
            .withBasicAuth(CLIENT_ID, CLIENT_SECRET)
            .withRequestBody(equalTo("grant_type=client_credentials"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        Json.write(
                            Map.of(
                                "access_token",
                                accessToken,
                                "token_type",
                                "Bearer",
                                "expires_in",
                                3600)))));

    WIRE_MOCK_SERVER.stubFor(
        WireMock.get(urlEqualTo("/issuer/keys"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(
                        Json.write(
                            Map.of("keys", List.of(RSA_KEY.toPublicJWK().toJSONObject()))))));
  }

  private static String createAccessToken() {
    try {
      Instant now = Instant.now();
      JWTClaimsSet claims =
          new JWTClaimsSet.Builder()
              .issuer(String.format("http://localhost:%d/issuer", WIRE_MOCK_SERVER.port()))
              .subject("john_doe")
              .jwtID(UUID.randomUUID().toString())
              .issueTime(Date.from(now))
              .expirationTime(Date.from(now.plusSeconds(3600)))
              .claim("scope", "kafka")
              .build();

      SignedJWT signedJwt =
          new SignedJWT(
              new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(RSA_KEY.getKeyID()).build(), claims);
      signedJwt.sign(new RSASSASigner(RSA_KEY.toRSAPrivateKey()));
      return signedJwt.serialize();
    } catch (JOSEException e) {
      throw new IllegalStateException("Failed to create OAuthBearer test token", e);
    }
  }

  private static RSAKey createRsaKey() {
    try {
      return new RSAKeyGenerator(2048)
          .keyID(UUID.randomUUID().toString())
          .keyUse(KeyUse.SIGNATURE)
          .generate();
    } catch (JOSEException e) {
      throw new IllegalStateException("Failed to create RSA key for OAuthBearer test token", e);
    }
  }
}
