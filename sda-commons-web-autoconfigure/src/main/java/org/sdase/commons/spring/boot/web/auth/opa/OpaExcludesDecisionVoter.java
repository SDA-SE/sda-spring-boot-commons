/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.opa;

import static org.springdoc.core.utils.Constants.API_DOCS_URL;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Stream;
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
      @Value(API_DOCS_URL) String apiDocsUrl) {
    if (excludedPathPatterns.isEmpty()) {
      this.excludedPathPatterns.add(Pattern.compile(apiDocsUrl + "\\.(json|yaml)"));
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
