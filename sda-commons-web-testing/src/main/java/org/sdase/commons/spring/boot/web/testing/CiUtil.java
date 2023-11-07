/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.testing;

import java.util.Set;

public class CiUtil {

  private final Set<String> envs;

  public CiUtil() {
    envs =
        Set.of(
            // Jenkins:
            // https://www.jenkins.io/doc/book/pipeline/jenkinsfile/#using-environment-variables
            "JENKINS_HOME",
            // Github Actions:
            // https://docs.github.com/en/actions/learn-github-actions/variables#default-environment-variables
            "CI");
  }

  CiUtil(Set<String> envs) {
    this.envs = envs;
  }

  /**
   * @return {@code true} if we can detect that the current build is executed in a CI pipeline
   */
  public boolean isRunningInCiPipeline() {
    return envs.stream().anyMatch(env -> lookup(env) != null);
  }

  private String lookup(String key) {
    var result = System.getProperty(key);
    if (result != null) {
      return result;
    } else return System.getenv(key);
  }
}
