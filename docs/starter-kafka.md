# Starter Kafka

The module `sda-commons-starter-kafka` provides autoconfigured Kafka producer and consumer
configuration.

Based on:

- `org.springframework.boot:spring-boot-starter`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.kafka:spring-kafka`

##  Configuration

| **Property**                                            | **Description**                                                                                                                   | **Default**        | **Example**                                     | **Env**                                              |
|---------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------|--------------------|-------------------------------------------------|------------------------------------------------------|
| `sda.kafka.consumer.retry.initialBackOffInterval` _int_ | The initial backoff of the retry in milli seconds.                                                                                | `1000`             | `1500`                                          | `SDA_KAFKA_CONSUMER_RETRY_INITIAL_BACKOFF_INTERVALL` |
| `sda.kafka.consumer.retry.maxBackOffInterval` _int_     | The max backoff interval  in milli seconds.                                                                                       | `4000`             | `5000`                                          | `SDA_KAFKA_CONSUMER_RETRY_MAX_BACKOFF_INTERVALL`     |
| `sda.kafka.consumer.retry.backOffMultiplier` _int_      | The multiplier beginning with the initial backoff.                                                                                | `2`                | `1.5`                                           | `SDA_KAFKA_CONSUMER_RETRY_INITIAL_BACKOFF_INTERVALL` |
| `sda.kafka.consumer.retry.maxRetries` _int_             | Max retries consuming the offset.                                                                                                 | `4`                | `10`                                            | `SDA_KAFKA_CONSUMER_RETRY_INITIAL_MAXRETRIES`        |
| `sda.kafka.consumer.dlt.pattern` _string_               | Pattern of consumer dead letter topic. `<topic>` will be replaced by topic name. If not set, `".DLT"` is added to the topic name. |                    | `"prefix-<topic>"`                              | `SDA_KAFKA_CONSUMER_DLT_PATTERN`                     |
| `management.health.kafka.enabled` _boolean_             | Flag to enable kafka health check.                                                                                                | `true`             | `false`                                         | `MANAGEMENT_HEALTH_KAFKA_ENABLED`                    |
| `management.health.kafka.timeout` _duration_            | Allowed duration for health check to finish.                                                                                      | `4s`               | `"5000ms"`                                      | `MANAGEMENT_HEALTH_KAFKA_TIMEOUT`                    |
| `spring.kafka.bootstrap.servers` _string_               | Comma-delimited list of `host:port` pairs to use for establishing the initial connections to the Kafka cluster.                   | `"localhost:9092"` | `"kafka-broker:9092"`                           | `SPRING_KAFKA_BOOTSTRAP_SERVERS`                     |
| `spring.kafka.security.protocol` _string_               | The security protocol used by Kafka. Please note that SASL mechanism requires some manual configuration.                          | `"PLAINTEXT"`      | `"SSL"`                                         | `SPRING_KAFKA_SECURITY_PROTOCOL`                     |
| `spring.kafka.ssl.keystore-location` _url_              | Location of the SSL keystore file.                                                                                                |                    | `"file:///kafka/kafka.client.keystore.jks"`     | `SPRING_KAFKA_SSL_KEYSTORELOCATION`                  |
| `spring.kafka.ssl.key-store-password` _string_          | Password for the SSL keystore file.                                                                                               |                    | `"s3cr3t"`                                      | `SPRING_KAFKA_SSL_KEYSTOREPASSWORD`                  |
| `spring.kafka.ssl.trust-store-location` _string_        | Location of the SSL truststore file.                                                                                              |                    | `"file:/kafka-certs/kafka.client.keystore.jks"` | `SPRING_KAFKA_SSL_TRUSTSTORELOCATION`                |
| `spring.kafka.ssl.trust-store-password` _string_        | Password for the SSL truststore file.                                                                                             |                    | `"s3cret"`                                      | `SPRING_KAFKA_SSL_TRUSTSTOREPASSWORD`                |
| `spring.kafka.consumer.group-id` _string_               | Consumer group name of Kafka Consumer.                                                                                            | `"default"`        | `"my-service-name"`                             | `SPRING_KAFKA_CONSUMER_GROUPID`                      |


**Make sure to overwrite `spring.kafka.consumer.group-id` in your `application.properties` otherwise
you could have conflicts with other services using `default`.**

For further information have a look to the Spring Kafka [reference documentation](https://docs.spring.io/spring-kafka/reference/html/).

??? info "Default configuration set by this library"
    ```properties
    --8<-- "sda-commons-starter-kafka/src/main/resources/org/sdase/commons/spring/boot/kafka/consumer.properties"

    --8<-- "sda-commons-starter-kafka/src/main/resources/org/sdase/commons/spring/boot/kafka/producer.properties"
    ```

## Consumer configuration

The autoconfigured consumer configuration provides
several `ConcurrentKafkaListenerContainerFactory<String, ?>`
which can be referenced in `@KafkaListener` annotated methods.

- `SdaKafkaListenerContainerFactory.LOG_ON_FAILURE`
  - Simply logs the exception; with a record listener, the remaining records from the previous poll
    are passed to the listener.
- `SdaKafkaListenerContainerFactory.RETRY_AND_LOG`
  - Skips record that keeps failing after `sda.kafka.consumer.retry.maxRetries`(default: `4`) and
    log exception.
- `SdaKafkaListenerContainerFactory.RETRY_AND_DLT`
  - Skips record that keeps failing after `sda.kafka.consumer.retry.maxRetries` (default: 4) and
    produces failed record to topic with `.DLT` suffix.
  - By default, the dead-letter record is sent to a topic named .DLT (the original topic name
    suffixed with .DLT) and to the same partition as the original record. Therefore, when you use
    the default configuration, **the dead-letter topic must have at least as many partitions as the
    original topic.**
  - The spring default DLT naming convention can be overwritten using the
    `sda.kafka.consumer.dlt.pattern` property.
    The pattern must contain `<topic>`, which will be replaced by the actual topic name.

To skip retry for specific business errors, you can throw the custom `NotRetryableKafkaException`.

Each `containerFactory` expects a message key as `String` and the message payload of any type.
The payload is deserialized as byte array and converted with the `ByteArrayJsonMessageConverter`.
The ack mode for offsets is per default `RECORD` where the offset after each record is
processed by the listener.

```java
@KafkaListener(
  topics = "TestTopic",
  containerFactory = SdaKafkaListenerContainerFactory.RETRY_AND_LOG)
public void retryAndLog(@Payload @Valid Message message) {
      // doSomething
    if(businessError) {
      throw new NotRetryableKafkaException();
    } 
}
```

## Producer configuration

The autoconfigured producer configuration provides a preconfigured  `KafkaTemplate` for producing 
messages with `String` key and payload as `json`.

You just need to autowire the `KafkaTemplate` and you are ready to go.
