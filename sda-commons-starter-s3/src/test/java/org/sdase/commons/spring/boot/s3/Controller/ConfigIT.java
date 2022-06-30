package org.sdase.commons.spring.boot.s3.Controller;

import static org.assertj.core.api.Assertions.assertThatCode;

import io.prometheus.client.CollectorRegistry;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.sdase.commons.spring.boot.s3.App;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ConfigIT {

  private static final Logger LOG = LoggerFactory.getLogger(ConfigIT.class);

  /**
   * Verifies that the config.yaml that is copied to the docker container is valid. If the
   * config.yaml uses required environment variables without defaults, they must be added to this
   * test using the {@link org.sdase.commons.server.testing.EnvironmentRule}.
   */
  @Disabled("this test says everything is fine even tho no config.yml exists (spring service)")
  @Test
  void verifyConfiguration() {
    try {
      Path path = Paths.get("src/main/jib/config.yml").toAbsolutePath();
      LOG.info("Testing config: {}", path);
      assertThatCode(() -> App.main(new String[] {"check", path.toString()}))
          .doesNotThrowAnyException();
    } finally {
      CollectorRegistry.defaultRegistry.clear();
    }
  }
}