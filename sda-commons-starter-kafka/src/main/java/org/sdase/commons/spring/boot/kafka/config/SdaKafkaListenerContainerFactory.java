/*
* Copyright (c). SDA SE Open Industry Solutions (https://www.sda.se).
*
* All rights reserved.
*/
package org.sdase.commons.spring.boot.kafka.config;

public class SdaKafkaListenerContainerFactory {

  private SdaKafkaListenerContainerFactory() {}

  /**
   * Skips record that keeps failing after {@code "sda.kafka.consumer.retry.maxRetries"}(default: 4)
   * and log exception.
   */
  public static final String RETRY_AND_LOG = "RETRY_AND_LOG";

  /**
   * Skips record that keeps failing after {@code sda.kafka.consumer.retry.maxRetries}(default: 4)
   * and produces failed record to topic with .DLT suffix.
   */
  public static final String RETRY_AND_DLT = "RETRY_AND_DLT";
  /**
   * Simply logs the exception; with a record listener, the remaining records from the previous poll
   * are passed to the listener.
   */
  public static final String LOG_ON_FAILURE = "LOG_ON_FAILURE";
}
