/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;

/**
 * Sets configured metadata fields to request headers. These headers will be passed to subsequents
 * requests
 */
public class MetadataContextClientInterceptor implements RequestInterceptor {

  private final Set<String> metadataFields;

  public MetadataContextClientInterceptor(Set<String> metadataFields) {
    this.metadataFields = metadataFields;
  }

  /**
   * Applies the configured metadata fields to the request headers of the {@link RequestTemplate}
   * parameter
   *
   * @param template Feign {@link RequestTemplate}
   */
  @Override
  public void apply(RequestTemplate template) {
    if (!metadataFields.isEmpty()) {
      MetadataContext metadataContext = MetadataContext.current();

      for (String metadataField : metadataFields) {
        List<String> valuesByKey = metadataContext.valuesByKey(metadataField);
        if (valuesByKey == null) {
          continue;
        }

        valuesByKey.stream()
            .filter(StringUtils::isNotBlank)
            .map(String::trim)
            .distinct()
            .forEach(v -> template.header(metadataField, v));
      }
    }
  }
}
