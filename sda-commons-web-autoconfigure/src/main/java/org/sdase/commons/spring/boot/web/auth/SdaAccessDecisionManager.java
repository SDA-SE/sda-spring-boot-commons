/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth;

import java.util.List;
import org.sdase.commons.spring.boot.web.auth.management.ManagementAccessDecisionVoter;
import org.sdase.commons.spring.boot.web.auth.opa.OpaAccessDecisionVoter;
import org.sdase.commons.spring.boot.web.auth.opa.OpaExcludesDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.stereotype.Component;

@Component
public class SdaAccessDecisionManager extends UnanimousBased {

  public SdaAccessDecisionManager(
      ManagementAccessDecisionVoter managementAccessDecisionVoter,
      OpaExcludesDecisionVoter opaExcludesDecisionVoter,
      OpaAccessDecisionVoter opaAccessDecisionVoter) {
    super(List.of(managementAccessDecisionVoter, opaExcludesDecisionVoter, opaAccessDecisionVoter));
  }
}
