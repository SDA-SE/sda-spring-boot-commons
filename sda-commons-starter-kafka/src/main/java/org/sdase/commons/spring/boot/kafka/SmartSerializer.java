package org.sdase.commons.spring.boot.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.support.serializer.JsonSerializer;
import java.nio.charset.StandardCharsets;

public class SmartSerializer<T> extends JsonSerializer<T> {
  public SmartSerializer(ObjectMapper objectMapper) {
    super(objectMapper);
  }

  @Override
  public byte[] serialize(String topic, T data) {
    if (data instanceof String dataStr) {
      return dataStr.getBytes(StandardCharsets.UTF_8);
    }
    return super.serialize(topic, data);
  }
}
