package com.epam.digital.data.platform.storage.form.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import org.springframework.core.serializer.support.DeserializingConverter;
import org.springframework.core.serializer.support.SerializingConverter;
import org.springframework.lang.Nullable;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SerializationService {

  private final ObjectMapper mapper;

  private final SerializingConverter serializingConverter = new SerializingConverter();
  private final DeserializingConverter deserializingConverter = new DeserializingConverter();

  public SerializationService(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public String serialize(@Nullable Object obj) {
    if (obj == null) {
      return "";
    }
    try {
      byte[] bytes = serializingConverter.convert(obj);
      String base64 = Base64.getEncoder().encodeToString(bytes);
      return mapper.writeValueAsString(base64);
    } catch (JsonProcessingException e) {
      log.info("Failed to wrap serialized payload as JSON", e);
      throw new IllegalArgumentException("Failed to wrap serialized payload as JSON");
    }
  }


  public Object deserialize(@Nullable String rawString) {
    if (rawString == null) {
      return null;
    }
    try {
      rawString = mapper.readValue(rawString, String.class);
      if (rawString == null) {
        return null;
      }
    } catch (JsonProcessingException e) {
      log.debug("Failed to parse JSON/base64 payload", e);
    }

    try {
      byte[] bytes = Base64.getDecoder().decode(rawString);
      return deserializingConverter.convert(bytes);
    } catch (IllegalArgumentException e) {
      log.info("Failed to parse base64 payload", e);
      return null;
    } catch (IllegalStateException e) {
      log.info("Failed to deserialize payload", e);
      return null;
    }
  }

}
