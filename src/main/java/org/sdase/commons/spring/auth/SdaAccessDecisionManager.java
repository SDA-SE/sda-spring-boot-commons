/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.auth;

import java.util.List;
import org.sdase.commons.spring.auth.opa.OpaAccessDecisionVoter;
import org.sdase.commons.spring.auth.opa.OpaExcludesDecisionVoter;
import org.springframework.security.access.vote.UnanimousBased;
import org.springframework.stereotype.Component;

@Component
public class SdaAccessDecisionManager extends UnanimousBased {

  public SdaAccessDecisionManager(
      OpaExcludesDecisionVoter opaExcludesDecisionVoter,
      OpaAccessDecisionVoter opaAccessDecisionVoter) {
    super(List.of(opaExcludesDecisionVoter, opaAccessDecisionVoter));
  }
}
