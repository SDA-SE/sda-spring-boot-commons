# sda-commons-kafka-starter


The `sda-commons-kafka-starter` provides several features to provide autoconfigured Kafka producer
and consumer configuration

##  Configuration

| **Property**                                            | **Description**                                                                | **Default** | **Example**                              | **Env**        |
|---------------------------------------------------------|--------------------------------------------------------------------------------|-------------|------------------------------------------|----------------|
| `sda.kafka.consumer.retry.initialBackOffInterval` _int_ |                                                                                |             | `https://iam-int.dev.de/auth/realms/123` | `AUTH_ISSUERS` |
| `sda.kafka.consumer.retry.maxBackOffInterval` _int_     | Comma separated string of open id discovery key sources with required issuers. |             | `https://iam-int.dev.de/auth/realms/123` | `AUTH_ISSUERS` |
| `sda.kafka.consumer.retry.backOffMultiplier` _int_      | Comma separated string of open id discovery key sources with required issuers. |             | `https://iam-int.dev.de/auth/realms/123` | `AUTH_ISSUERS` |
| `sda.kafka.consumer.retry.maxRetries` _int_             | Comma separated string of open id discovery key sources with required issuers. |             | `https://iam-int.dev.de/auth/realms/123` | `AUTH_ISSUERS` |


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

## Producer configuration

The autoconfigured producer configuration provides a preconfigured  `KafkaTemplate` for producing 
messages with `String` key and payload as `json`.

You just need to autowire the `KafkaTemplate` and you are ready to go.

### Defaults 

```properties
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.springframework.kafka.support.serializer.JsonSerializer
```


