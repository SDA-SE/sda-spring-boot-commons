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
import org.sdase.commons.spring.boot.metadata.context.DetachedMetadataContext;
import org.sdase.commons.spring.boot.metadata.context.MetadataContext;
import org.springframework.kafka.listener.RecordInterceptor;

public class MetadataContextRecordInterceptor<K, V> implements RecordInterceptor<K, V> {

  private final Set<String> metadataFields = MetadataContext.metadataFields();

  @Override
  public ConsumerRecord<K, V> intercept(ConsumerRecord<K, V> record, Consumer<K, V> consumer) {
    MetadataContext.createContext(createMetadataContext(record));
    return record;
  }

  @Override
  public void afterRecord(ConsumerRecord consumerRecord, Consumer consumer) {
    MetadataContext.createContext(new DetachedMetadataContext());
  }

  private DetachedMetadataContext createMetadataContext(ConsumerRecord consumerRecord) {
    var newContext = new DetachedMetadataContext();
    var headers = consumerRecord.headers();
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
    return newContext;
  }
}
