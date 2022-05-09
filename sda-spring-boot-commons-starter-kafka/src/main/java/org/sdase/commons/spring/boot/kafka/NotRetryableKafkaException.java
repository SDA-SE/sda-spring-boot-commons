/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.kafka;

import org.springframework.kafka.listener.ConsumerRecordRecoverer;

/**
 * This exception is considered to be fatal, and retries are skipped for such exceptions. The
 * {@linkplain ConsumerRecordRecoverer} is invoked on the first failure, when the bean is provided
 * for context.
 */
public class NotRetryableKafkaException extends RuntimeException {

  public NotRetryableKafkaException() {}

  public NotRetryableKafkaException(String message) {
    super(message);
  }

  public NotRetryableKafkaException(String message, Throwable cause) {
    super(message, cause);
  }

  public NotRetryableKafkaException(Throwable cause) {
    super(cause);
  }

  public NotRetryableKafkaException(
      String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}
