/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.web.metadata;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import org.apache.commons.lang3.StringUtils;

/**
 * A {@link ClientRequestFilter} to submit the {@link MetadataContext} to other services that are
 * called synchronously.
 */
public class MetadataContextClientRequestFilter implements ClientRequestFilter {

  private final Set<String> metadataFields;

  public MetadataContextClientRequestFilter(Set<String> metadataFields) {
    this.metadataFields = metadataFields;
  }

  @Override
  public void filter(ClientRequestContext requestContext) throws IOException {
    if (!metadataFields.isEmpty()) {
      MetadataContext metadataContext = MetadataContext.current();
      var headers = requestContext.getHeaders();
      for (String metadataField : metadataFields) {
        List<String> valuesByKey = metadataContext.valuesByKey(metadataField);
        if (valuesByKey == null) {
          continue;
        }
        valuesByKey.stream()
            .filter(StringUtils::isNotBlank)
            .map(String::trim)
            .distinct()
            .forEach(v -> headers.add(metadataField, v));
      }
    }
  }
}
