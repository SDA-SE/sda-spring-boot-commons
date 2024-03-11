/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerInterceptor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.MDC;

public class TraceTokenProducerInterceptor<K, V> implements ProducerInterceptor<K, V> {

  @Override
  public ProducerRecord<K, V> onSend(ProducerRecord<K, V> record) {

    String mdcTraceToken = MDC.get("Trace-Token");
    if (mdcTraceToken != null && !mdcTraceToken.isBlank()) {
      record.headers().add("Parent-Trace-Token", mdcTraceToken.getBytes(StandardCharsets.UTF_8));
    }

    return record;
  }

  @Override
  public void onAcknowledgement(RecordMetadata metadata, Exception exception) {}

  @Override
  public void close() {}

  @Override
  public void configure(Map<String, ?> configs) {}
}
