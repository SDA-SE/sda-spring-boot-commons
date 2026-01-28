/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.auth.opa;

import static org.springdoc.core.utils.Constants.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
public class OpaExcludesAuthorizationManager
    implements AuthorizationManager<RequestAuthorizationContext> {

  private final Set<Pattern> excludedPathPatterns = new HashSet<>();

  public OpaExcludesAuthorizationManager(
      @Value("${opa.exclude.patterns:}") String excludedPathPatterns,
      @Value(API_DOCS_URL) String apiDocsUrl,
      @Value(DEFAULT_API_DOCS_URL_YAML) String apiDocsUrlYaml) {
    if (!excludedPathPatterns.isEmpty()) {
      Stream.of(excludedPathPatterns.split(","))
          .filter(Objects::nonNull)
          .filter(p -> !p.isBlank())
          .map(String::trim)
          .map(Pattern::compile)
          .forEach(this.excludedPathPatterns::add);
    }

    // Always exclude the apiDocsUrls
    this.excludedPathPatterns.add(Pattern.compile(apiDocsUrl));
    this.excludedPathPatterns.add(Pattern.compile(apiDocsUrlYaml));
  }

  @Override
  public void verify(
      Supplier<? extends @Nullable Authentication> authentication,
      RequestAuthorizationContext object) {
    AuthorizationManager.super.verify(authentication, object);
  }

  @Override
  public @Nullable AuthorizationResult authorize(
      Supplier<? extends @Nullable Authentication> authentication,
      RequestAuthorizationContext object) {
    if (object == null) {
      return new AuthorizationDecision(false);
    }
    var httpRequest = object.getRequest();
    var path = httpRequest.getServletPath();
    return excludedPathPatterns.stream().anyMatch(p -> p.matcher(path).matches())
        ? new AuthorizationDecision(true)
        : new AuthorizationDecision(false);
  }
}
