/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka;

import java.util.Arrays;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;

public class TraceTokenRecordInterceptor<K, V> implements RecordInterceptor<K, V> {

  @Override
  public ConsumerRecord<K, V> intercept(ConsumerRecord<K, V> record, Consumer<K, V> consumer) {

    Arrays.stream(record.headers().toArray())
        .filter(header -> header.key().equals("Parent-Trace-Token"))
        .findFirst()
        .ifPresent(header -> MDC.put("Parent-Trace-Token", new String(header.value())));

    return record;
  }

  @Override
  public void afterRecord(ConsumerRecord<K, V> record, Consumer<K, V> consumer) {
    MDC.remove("Parent-Trace-Token");
  }
}
