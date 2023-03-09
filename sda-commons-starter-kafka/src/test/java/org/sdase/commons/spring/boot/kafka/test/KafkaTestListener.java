/*
 * Copyright 2022- SDA SE Open Industry Solutions (https://www.sda.se)
 *
 * Use of this source code is governed by an MIT-style
 * license that can be found in the LICENSE file or at
 * https://opensource.org/licenses/MIT.
 */
package org.sdase.commons.spring.boot.kafka.test;

import javax.validation.Valid;
import org.sdase.commons.spring.boot.kafka.NotRetryableKafkaException;
import org.sdase.commons.spring.boot.kafka.config.SdaKafkaListenerContainerFactory;
import org.sdase.commons.spring.boot.web.metadata.MetadataContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class KafkaTestListener {

  private static final Logger LOG = LoggerFactory.getLogger(KafkaTestListener.class);
  private final ListenerCheck listenerCheck;
  private final MetadataCollector metadataCollector;

  public KafkaTestListener(ListenerCheck listenerCheck, MetadataCollector metadataCollector) {
    this.listenerCheck = listenerCheck;
    this.metadataCollector = metadataCollector;
  }

  @KafkaListener(
      topics = "${app.kafka.consumer.retry-and-log.topic}",
      containerFactory = SdaKafkaListenerContainerFactory.RETRY_AND_LOG)
  public void retryAndLog(@Payload @Valid KafkaTestModel kafkaTestModel) {
    listenerCheck.check(kafkaTestModel.getCheckString());
    throwExceptionIfDesired(kafkaTestModel);
  }

  @KafkaListener(
      topics = "${app.kafka.consumer.retry-and-dlt.topic}",
      containerFactory = SdaKafkaListenerContainerFactory.RETRY_AND_DLT)
  public void retryAndDlt(@Payload @Valid KafkaTestModel kafkaTestModel) {
    listenerCheck.check(kafkaTestModel.getCheckString());
    throwExceptionIfDesired(kafkaTestModel);
  }

  @KafkaListener(
      topics = "${app.kafka.consumer.log-on-failure.topic}",
      containerFactory = SdaKafkaListenerContainerFactory.LOG_ON_FAILURE)
  public void logOnFailure(@Payload @Valid KafkaTestModel kafkaTestModel) {
    listenerCheck.check(kafkaTestModel.getCheckString());
    throwExceptionIfDesired(kafkaTestModel);
  }

  @KafkaListener(
      topics = "${app.kafka.consumer.metadata.topic}",
      containerFactory = SdaKafkaListenerContainerFactory.RETRY_AND_LOG)
  public void consumeMetadata(@Payload @Valid KafkaTestModel kafkaTestModel) {
    metadataCollector.setLastCollectedContext(MetadataContext.detachedCurrent());
  }

  @Component
  public static class ListenerCheck {

    public void check(String checkString) {
      // DO NOTING
    }
  }

  private void throwExceptionIfDesired(KafkaTestModel kafkaTestModel) {
    if (kafkaTestModel.isThrowNotRetryableException()) {
      throw new NotRetryableKafkaException();
    }
    if (kafkaTestModel.isThrowRuntimeException()) {
      throw new RuntimeException();
    }
  }
}
