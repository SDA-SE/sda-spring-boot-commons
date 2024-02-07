# Starter Kafka

The module `sda-commons-starter-kafka` provides autoconfigured Kafka producer and consumer
configuration.

Based on:

- `org.springframework.boot:spring-boot-starter`
- `org.springframework.boot:spring-boot-starter-validation`
- `org.springframework.kafka:spring-kafka`

##  Configuration

--8<-- "doc-snippets/config-starter-kafka.md"

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

### DLT Error Handling

To allow handling of serialization exceptions, the DLT KafkaTemplate is using a ByteArraySerializer.
You can add additional templates used for uploading messages to the dead-letter-topic for records
that were deserialized successfully, by overwriting the `dltTemplates` bean. 
see [spring-kafka documentation](https://docs.spring.io/spring-kafka/reference/kafka/annotation-error-handling.html#dead-letters).

```java
@Configuration
@Import({SdaKafkaConsumerConfiguration.class})
public class DltConfiguration {

  @Autowired
  @Qualifier("kafkaByteArrayDltTemplate")
  private KafkaTemplate<String, ?> recoverTemplate;

  @Autowired
  private KafkaTemplate<String, ?> myCustomTemplate;


  @Bean
  public Map<Class<?>, KafkaOperations<?, ?>> dltTemplates() {
    Map<Class<?>, KafkaOperations<?, ?>> templates = new LinkedHashMap<>();
    templates.put(MyCustomClass.class, myCustomTemplate);
    templates.put(byte[].class, recoverTemplate);
    return templates;
  }
}
```

## Producer configuration

The autoconfigured producer configuration provides a preconfigured  `KafkaTemplate` for producing 
messages with `String` key and payload as `json`.

To configure different serializers, use `spring.kafka.producer.key-serializer` and 
`spring.kafka.producer.value-serializer` properties

!!! warning "ProducerFactory"
    Do not hard code `((DefaultKafkaProducerFactory<?, ?>) producerFactory)
    .setValueSerializer(new JsonSerializer<>(objectMapper));` of the default spring producer factory. 
    It will affect other configurations, we recommend to use a copy of the producer factory as in the example below.

```java
@Bean("kafkaByteArrayDltTemplate")  
public KafkaTemplate<String, ?> kafkaByteArrayDltTemplate(ProducerFactory<String, ?> producerFactory) {

  Map<String, Object> props = new HashMap<>(commonProperties);
  props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
  props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
  ProducerFactory<String, ?> producerFactoryByte =
      producerFactory.copyWithConfigurationOverride(props);


  return new KafkaTemplate<>(producerFactoryByte, props);
}
```

You need to autowire the KafkaTemplate using a Qualifier.

```java
@Qualifier("kafkaByteArrayDltTemplate") KafkaTemplate<String, ?> recoverTemplate,
```

