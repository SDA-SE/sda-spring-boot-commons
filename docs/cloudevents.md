# CloudEvents

The module `sda-commons-cloudevents` provides some glue code to work with
[CloudEvents](https://cloudevents.io/) on top of Apache Kafka.


## Introduction

CloudEvents is a general standard that can be used in combination with your favourite eventing tool
like ActiveMQ or Kafka.
The CloudEvents specification defines concrete bindings to define how the general specification
should be applied to a specific tool.
This module provides POJOs to use [CloudEvent's structured content mode](https://github.com/cloudevents/spec/blob/v1.0.2/cloudevents/bindings/kafka-protocol-binding.md#33-structured-content-mode).


## Producing CloudEvents

For simplicity, we recommend to extend our base class and add your own class for the `data`
property.
Additional documentation can be added at class level and for the data type.

!!! example "Custom event"

    ```java
    --8<-- "sda-commons-cloudevents/src/test/java/org/sdase/commons/spring/boot/cloudevents/app/partner/PartnerCreatedEvent.java"
    ```

You can use a standard `org.springframework.kafka.support.serializer.JsonSerializer` to publish the
event.


## Consuming CloudEvents

You can use `org.springframework.kafka.support.serializer.JsonDeserializer` to consume CloudEvents.


## Polymorphism

Usually, an eventing API consists of multiple events related to one aggregate.
You can use Jacksons subtype features to define multiple events in one model.

!!! example "Multiple events in one API"
    ```java
    --8<-- "sda-commons-cloudevents/src/test/java/org/sdase/commons/spring/boot/cloudevents/app/polymorphism/CarLifecycleEvents.java"
    ```
    _Note: If such a model grows, you may want to extract the data types as top level classes._ 
