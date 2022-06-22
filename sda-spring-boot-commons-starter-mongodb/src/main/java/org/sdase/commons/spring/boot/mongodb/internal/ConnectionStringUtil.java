package org.sdase.commons.spring.boot.mongodb.internal;

import io.micrometer.core.instrument.util.StringUtils;
import org.sdase.commons.spring.boot.mongodb.MongoConfiguration;

public class ConnectionStringUtil {
  private ConnectionStringUtil() {
    // this is a utility class
  }

  static String createConnectionString(MongoConfiguration configuration) {

    StringBuilder connectionStringBuilder = new StringBuilder();

    connectionStringBuilder
        .append("mongodb://")
        .append(buildCredentialsUriPartIfNeeded(configuration))
        .append(configuration.getHosts())
        .append("/")
        .append(configuration.getDatabase());

    if (StringUtils.isNotBlank(configuration.getOptions())) {
      connectionStringBuilder.append("?").append(configuration.getOptions());
    }

    return connectionStringBuilder.toString();
  }

  private static StringBuilder buildCredentialsUriPartIfNeeded(MongoConfiguration configuration) {
    if (StringUtils.isNotBlank(configuration.getUsername())
        && StringUtils.isNotBlank(configuration.getPassword())) {
      return new StringBuilder()
          .append(configuration.getUsername())
          .append(":")
          .append(configuration.getPassword())
          .append("@");
    }
    return new StringBuilder();
  }
}
