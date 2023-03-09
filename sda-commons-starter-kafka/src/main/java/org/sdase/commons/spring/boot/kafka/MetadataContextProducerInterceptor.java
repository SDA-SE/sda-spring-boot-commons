/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.sdase.commons.spring.boot.metadata.context.MetadataContext;

public class MetadataContextProducerInterceptor<K, V> implements ProducerInterceptor<K, V> {

  private Set<String> metadataFields;

  @Override
  public ProducerRecord<K, V> onSend(ProducerRecord<K, V> producerRecord) {
    var headers = new RecordHeaders(producerRecord.headers());
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
          .map(v -> v.getBytes(StandardCharsets.UTF_8))
          .forEach(v -> headers.add(metadataField, v));
    }

    return new ProducerRecord<>(
        producerRecord.topic(),
        producerRecord.partition(),
        producerRecord.timestamp(),
        producerRecord.key(),
        producerRecord.value(),
        headers);
  }

  @Override
  public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
    // not used
  }

  @Override
  public void close() {
    // not used
  }

  @Override
  public void configure(Map<String, ?> configs) {
    metadataFields = MetadataContext.metadataFields();
  }
}
