# sda-commons-kafka-starter

The `sda-commons-kafka-starter` provides autoconfigured Kafka producer and consumer configuration.

Based on:
- `org.springframework.boot:spring-boot-starter`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.kafka:spring-kafka`

##  Configuration

| **Property**                                            | **Description**                                   | **Default** | **Example** | **Env**                                              |
|---------------------------------------------------------|---------------------------------------------------|-------------|-------------|------------------------------------------------------|
| `sda.kafka.consumer.retry.initialBackOffInterval` _int_ | The initial backoff of the retry in ms            | `1000`      | `1500`      | `SDA_KAFKA_CONSUMER_RETRY_INITIAL_BACKOFF_INTERVALL` |
| `sda.kafka.consumer.retry.maxBackOffInterval` _int_     | The max backoff interval  in ms                   | `4000`      | `5000`      | `SDA_KAFKA_CONSUMER_RETRY_MAX_BACKOFF_INTERVALL`     |
| `sda.kafka.consumer.retry.backOffMultiplier` _int_      | The multiplier beginning with the initial backoff | `2`         | `1.5`       | `SDA_KAFKA_CONSUMER_RETRY_INITIAL_BACKOFF_INTERVALL` |
| `sda.kafka.consumer.retry.maxRetries` _int_             | Max retries consuming the offset                  | `4`         | `10`        | `SDA_KAFKA_CONSUMER_RETRY_INITIAL_BACKOFF_INTERVALL` |

For further information have a look to the Spring Kafka [reference documentation](https://docs.spring.io/spring-kafka/reference/html/).

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
    the default configuration, the dead-letter topic must have at least as many partitions as the
    original topic.

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

### Defaults

```properties
spring.kafka.consumer.group-id=default
spring.kafka.consumer.auto-offset-reset=earliest
spring.kafka.consumer.key-deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
spring.kafka.consumer.value-deserializer=org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
spring.kafka.consumer.properties.spring.deserializer.key.delegate.class=org.apache.kafka.common.serialization.StringDeserializer
spring.kafka.consumer.properties.spring.deserializer.value.delegate.class=org.apache.kafka.common.serialization.ByteArrayDeserializer
spring.kafka.consumer.properties.spring.json.trusted.packages=*
```

**Make sure to overwrite `spring.kafka.consumer.group-id` in your `application.properties` otherwise you could have conflicts
with other services using `default`.**

## Producer configuration

The autoconfigured producer configuration provides a preconfigured  `KafkaTemplate` for producing 
messages with `String` key and payload as `json`.

You just need to autowire the `KafkaTemplate` and you are ready to go.

### Defaults 

```properties
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```

## Configuration properties

* `spring.kafka.bootstrap.servers` _string_
  * Comma-delimited list of `host:port` pairs to use for establishing the initial connections to the
    Kafka cluster
  * Example:  `kafka-broker:9092"`
  * Default: `localhost:9092`
* `spring.kafka.security.protocol` _string_
  * The security protocol used by Kafka. Please note that SASL mechanism requires some manual
    configuration.
  * Example: `PLAINTEXT` or `SSL`
  * Default: `PLAINTEXT`
* `spring.kafka.ssl.key-store-location` _string_
  * Location of the SSL keystore file
  * Example: `file:/kafka/kafka.client.keystore.jks`
  * Default: ``
* `spring.kafka.ssl.key-store-password` _string_
  * Password for the SSL keystore file
  * Example: `s3cret`
  * Default: ``
* `spring.kafka.ssl.trust-store-location´` _string_
  * Location of the SSL truststore file
  * Example: `file:/kafka-certs/kafka.client.keystore.jks`
  * Default: ``
* `Sspring.kafka.ssl.trust-store-password` _string_
  * Password for the SSL truststore file
  * Example: `s3cret`
  * Default: ``

#### Consumers

* `spring.kafka.consumer.group-id` _string_
  * Consumer group name of Kafka Consumer
  * Example: `myConsumer`
  * Default: `default`
