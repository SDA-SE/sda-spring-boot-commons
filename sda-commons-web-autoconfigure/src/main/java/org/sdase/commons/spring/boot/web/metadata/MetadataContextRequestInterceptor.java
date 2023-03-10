/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.sdase.commons.spring.boot.metadata.context.DetachedMetadataContext;
import org.sdase.commons.spring.boot.metadata.context.MetadataContext;
import org.sdase.commons.spring.boot.metadata.context.MetadataContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * A {@link HandlerInterceptor} to submit the {@link MetadataContext} to other services that are
 * called synchronously.
 */
public class MetadataContextRequestInterceptor implements HandlerInterceptor {

  private final Set<String> metadataFields;

  public MetadataContextRequestInterceptor(Set<String> metadataFields) {
    this.metadataFields = metadataFields;
  }

  /**
   * Builds a {@link DetachedMetadataContext} with the variables specified in the configuration and
   * the headers present on request headers
   *
   * @param request current HTTP request
   * @param response current HTTP response
   * @param handler chosen handler to execute, for type and/or instance evaluation
   * @return true
   */
  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) {
    var metadataContext = buildContext(request);
    MetadataContextHolder.set(metadataContext.toMetadataContext());
    return true;
  }

  /**
   * Clears the MetadataContext for the current request and response
   *
   * @param request current HTTP request
   * @param response current HTTP response
   * @param handler the handler (or {@link HandlerMethod}) that started asynchronous execution, for
   *     type and/or instance examination
   * @param ex any exception thrown on handler execution, if any; this does not include exceptions
   *     that have been handled through an exception resolver
   */
  @Override
  public void afterCompletion(
      HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    MetadataContextHolder.clear();
  }

  private DetachedMetadataContext buildContext(HttpServletRequest request) {
    var metadataContext = new DetachedMetadataContext();
    for (String metadataField : metadataFields) {
      List<String> values = normalize(request.getHeaders(metadataField));
      metadataContext.put(metadataField, values);
    }
    return metadataContext;
  }

  private List<String> normalize(Enumeration<String> original) {
    if (original == null) {
      return List.of();
    }
    return Collections.list(original).stream()
        .filter(StringUtils::isNotBlank)
        .map(s -> s.split(","))
        .flatMap(Stream::of)
        .filter(StringUtils::isNotBlank)
        .map(String::trim)
        .toList();
  }
}
