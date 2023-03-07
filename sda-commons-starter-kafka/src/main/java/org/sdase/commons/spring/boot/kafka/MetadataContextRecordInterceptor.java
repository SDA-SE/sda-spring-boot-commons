/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.Set;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.sdase.commons.spring.boot.web.metadata.DetachedMetadataContext;
import org.sdase.commons.spring.boot.web.metadata.MetadataContext;
import org.sdase.commons.spring.boot.web.metadata.MetadataContextCloseable;
import org.springframework.kafka.listener.RecordInterceptor;

@SuppressWarnings("java:S6213") // Suppress warnings for restricted identifiers
public class MetadataContextRecordInterceptor implements RecordInterceptor {

  private final Set<String> metadataFields = MetadataContext.metadataFields();
  private MetadataContextCloseable metadataContextCloseable;

  @Override
  public ConsumerRecord<String, Object> intercept(ConsumerRecord record, Consumer consumer) {
    metadataContextCloseable = createMetadataContext(record);
    return record;
  }

  @Override
  public ConsumerRecord intercept(ConsumerRecord record) {
    metadataContextCloseable = createMetadataContext(record);
    return record;
  }

  @Override
  public void afterRecord(ConsumerRecord record, Consumer consumer) {
    metadataContextCloseable.close();
  }

  private MetadataContextCloseable createMetadataContext(ConsumerRecord record) {
    var newContext = new DetachedMetadataContext();
    var headers = record.headers();
    for (var field : metadataFields) {
      var values =
          StreamSupport.stream(headers.headers(field).spliterator(), false)
              .map(Header::value)
              .map(v -> new String(v, UTF_8))
              .filter(StringUtils::isNotBlank)
              .map(String::trim)
              .toList();
      newContext.put(field, values);
    }
    return MetadataContext.createCloseableContext(newContext);
  }
}
