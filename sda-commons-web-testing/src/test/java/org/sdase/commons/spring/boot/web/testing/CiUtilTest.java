/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

class CiUtilTest {

  private final CiUtil ciUtil = new CiUtil(Set.of("RUN_IN_CI"));

  @Test
  @SetSystemProperty(key = "RUN_IN_CI", value = "yeah")
  void shouldAssumeItRunsInCiWhenEnvMatches() {
    assertThat(ciUtil.isRunningInCiPipeline()).isTrue();
  }

  @Test
  void shouldNotAssumeItRunsInCiWhenEnvIsNotPresent() {
    assertThat(ciUtil.isRunningInCiPipeline()).isFalse();
  }
}
