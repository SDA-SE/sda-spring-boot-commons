package org.sdase.commons.spring.boot.mongodb.internal;

import com.mongodb.*;
import io.opentracing.Tracer;
import io.opentracing.contrib.mongo.common.SpanDecorator;
import io.opentracing.contrib.mongo.common.TracingCommandListener;
import io.opentracing.util.GlobalTracer;
import org.sdase.commons.spring.boot.mongodb.MongoConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import javax.net.ssl.SSLContext;

import java.util.Arrays;

import static java.util.Arrays.asList;
import static org.sdase.commons.spring.boot.mongodb.internal.ConnectionStringUtil.createConnectionString;

public class MongoClientBuilder {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoClientBuilder.class);

  private static final MongoClientOptions.Builder DEFAULT_OPTIONS =
      MongoClientOptions.builder().writeConcern(WriteConcern.ACKNOWLEDGED);

  private final MongoConfiguration configuration;
  private final MongoClientOptions.Builder mongoClientOptionsBuilder;
  private Tracer tracer;
  private SSLContext sslContext;

  public MongoClientBuilder(MongoConfiguration configuration) {
    this(configuration, MongoClientOptions.builder(DEFAULT_OPTIONS.build()));
  }

  MongoClientBuilder(
      MongoConfiguration configuration, MongoClientOptions.Builder mongoClientOptionsBuilder) {
    this.configuration = configuration;
    this.mongoClientOptionsBuilder = mongoClientOptionsBuilder;
  }

  public MongoClientBuilder withTracer(Tracer tracer) {
    this.tracer = tracer;
    return this;
  }

  public MongoClientBuilder withSSlContext(SSLContext sslContext) {
    this.sslContext = sslContext;
    return this;
  }

  /**
   * build mongo client for environment
   *
   * @param environment the dropwizard environment of the current application
   * @return a {@link MongoClient} that can access the configured database
   */
  public MongoClient build(Environment environment) {

    if (configuration == null) {
      throw new IllegalArgumentException("configuration is required");
    }
    try {
      LOGGER.info("Connecting to MongoDB at '{}'", configuration.getHosts());
      final MongoClient mongoClient = createMongoClient();
      Arrays.stream(environment.getActiveProfiles()).onClose(mongoClient::close);
      //environment.lifecycle().manage(onShutdown(mongoClient::close));
      LOGGER.info("Connected to MongoDB at '{}'", configuration.getHosts());
      return mongoClient;
    } catch (Exception e) {
      throw new MongoException("Could not configure MongoDB client.", e);
    }
  }

  private MongoClient createMongoClient() {
    if (configuration.isUseSsl()) {
      mongoClientOptionsBuilder.sslEnabled(true);
      // use sslContext created with env variable by default

     /* if (StringUtils.isNotBlank(configuration.getCaCertificate())) {
        LOGGER.info("Overriding ssl config from env variable");
        sslContext = createSslContextIfAnyCertificatesAreConfigured();
      } */

      if (sslContext != null) {
        mongoClientOptionsBuilder.sslContext(sslContext);
      }
    }

    // Initialize a tracer that traces all calls to the MongoDB server.
    Tracer currentTracer = tracer == null ? GlobalTracer.get() : tracer;
    TracingCommandListener listener =
        new TracingCommandListener.Builder(currentTracer)
            .withSpanDecorators(asList(SpanDecorator.DEFAULT, new NoStatementSpanDecorator()))
            .build();
    mongoClientOptionsBuilder.addCommandListener(listener);

    return new MongoClient(
        new MongoClientURI(createConnectionString(configuration), mongoClientOptionsBuilder));
  }

  /*
  private SSLContext createSslContextIfAnyCertificatesAreConfigured() {
    String caCertificate = configuration.getCaCertificate();
    KeyStore truststoreFromPemKey = SslUtil.createTruststoreFromPemKey(caCertificate);
    return SslUtil.createSslContext(truststoreFromPemKey);
  }

   */
}
