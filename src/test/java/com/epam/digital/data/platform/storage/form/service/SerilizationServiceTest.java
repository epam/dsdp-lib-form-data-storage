package com.epam.digital.data.platform.storage.form.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.serializer.support.SerializationFailedException;

class SerilizationServiceTest {

  private SerializationService service;
  private ObjectMapper mapper;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper();
    service = new SerializationService(mapper);
  }

  @Test
  void serialize_shouldReturnJsonQuotedBase64_forString() {
    String input = "hello";
    String serialized = service.serialize(input);

    assertTrue(serialized.startsWith("\"") && serialized.endsWith("\""),
        "Serialized output must be a JSON string (quoted)");

    Object deserialized = service.deserialize(serialized);
    assertEquals(input, deserialized);
  }

  @Test
  void serializeAndDeserialize_roundTrip_forCustomSerializable() {
    MyPojo input = new MyPojo(42, "answer");
    String serialized = service.serialize(input);

    assertTrue(serialized.startsWith("\"") && serialized.endsWith("\""),
        "Serialized output must be a JSON string (quoted)");

    Object deserialized = service.deserialize(serialized);
    assertInstanceOf(MyPojo.class, deserialized);
    MyPojo pojo = (MyPojo) deserialized;
    assertEquals(42, pojo.id());
    assertEquals("answer", pojo.name());
  }

  @Test
  void serialize_shouldThrowIllegalArgumentException_whenObjectIsNotSerializable() {
    class NotSerializable {
      int x = 1;
    }

    NotSerializable obj = new NotSerializable();

    assertThrows(SerializationFailedException.class, () -> service.serialize(obj));
  }

  @Test
  void deserialize_shouldReturnNull_whenRawStringIsNull() {
    assertNull(service.deserialize(null));
  }

  @Test
  void deserialize_shouldReturnNull_whenRawStringIsNotValidJson() {
    String invalidJson = "{not a json string}";
    assertNull(service.deserialize(invalidJson));
  }

  @Test
  void deserialize_shouldReturnNull_whenBase64IsInvalid() throws Exception {
    String notBase64 = "###NOT-BASE64###";
    String jsonString = mapper.writeValueAsString(notBase64);

    assertNull(service.deserialize(jsonString));
  }

  @Test
  void deserialize_shouldReturnOriginal_whenGivenValidJsonQuotedBase64() {
    MyPojo input = new MyPojo(7, "seven");

    String serialized = service.serialize(input);
    Object output = service.deserialize(serialized);

    assertInstanceOf(MyPojo.class, output);
    MyPojo pojo = (MyPojo) output;
    assertEquals(7, pojo.id());
    assertEquals("seven", pojo.name());
  }

  @Test
  void deserialize_optionalExamples_durationValue() {
    Duration d = Duration.ofSeconds(30);
    String serialized = service.serialize(d);
    Object out = service.deserialize(serialized);
    assertInstanceOf(Duration.class, out);
    assertEquals(d, out);
  }

  private record MyPojo(int id, String name) implements Serializable {
  }
}

