/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.auth.opa;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.springdoc.core.SpringDocConfigProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDecisionVoter;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.FilterInvocation;
import org.springframework.stereotype.Component;

@Component
public class OpaExcludesDecisionVoter implements AccessDecisionVoter<FilterInvocation> {

  private final Set<Pattern> excludedPathPatterns = new HashSet<>();

  public OpaExcludesDecisionVoter(
      @Value("${opa.exclude.patterns:}") String excludedPathPatterns,
      SpringDocConfigProperties springDocConfigProperties) {
    if (excludedPathPatterns.isEmpty()) {
      var configuredSpringDocPath = springDocConfigProperties.getApiDocs().getPath();
      this.excludedPathPatterns.add(Pattern.compile(configuredSpringDocPath + "\\.(json|yaml)"));
    } else {
      Stream.of(excludedPathPatterns.split(","))
          .filter(Objects::nonNull)
          .filter(p -> !p.isBlank())
          .map(String::trim)
          .map(Pattern::compile)
          .forEach(this.excludedPathPatterns::add);
    }
  }

  @Override
  public boolean supports(ConfigAttribute attribute) {
    return true;
  }

  @Override
  public boolean supports(Class<?> clazz) {
    return FilterInvocation.class.isAssignableFrom(clazz);
  }

  @Override
  public int vote(
      Authentication authentication,
      FilterInvocation filterInvocation,
      Collection<ConfigAttribute> attributes) {
    var httpRequest = filterInvocation.getHttpRequest();
    var path = httpRequest.getServletPath();
    return excludedPathPatterns.stream().anyMatch(p -> p.matcher(path).matches())
        ? ACCESS_GRANTED
        : ACCESS_ABSTAIN;
  }
}
