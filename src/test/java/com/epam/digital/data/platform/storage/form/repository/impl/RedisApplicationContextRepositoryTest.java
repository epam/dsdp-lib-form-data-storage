/*
 * Copyright 2025 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.storage.form.repository.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.atLeastOnce;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RedisApplicationContextRepository Tests")
class RedisApplicationContextRepositoryTest {

  private static final String KEYSPACE = "test-keyspace";
  private static final String REDIS_KEY_SUFFIX = "test-suffix";
  private static final String CONTEXT_ID = "test-context-id";
  private static final String ENTRY_NAME = "test-entry-name";
  private static final String EXPECTED_CONTEXT_KEY = "test-keyspace:application:test-context-id" +
      ":context:test-suffix";

  @Mock
  private RedisTemplate<String, Object> redisTemplate;

  @Mock
  private HashOperations<String, Object, Object> hashOperations;

  private RedisApplicationContextRepository repository;

  @BeforeEach
  void setUp() {
    repository = new RedisApplicationContextRepository(redisTemplate, KEYSPACE, REDIS_KEY_SUFFIX);
    lenient().when(redisTemplate.opsForHash()).thenReturn(hashOperations);
  }

  @Nested
  @DisplayName("Put Operations")
  class PutOperations {

    @Test
    @DisplayName("should put entry into context")
    void shouldPutEntryIntoContext() {
      String value = "test-value";

      repository.put(CONTEXT_ID, ENTRY_NAME, value);

      verify(hashOperations).put(EXPECTED_CONTEXT_KEY, ENTRY_NAME, value);
    }

    @Test
    @DisplayName("should put complex object into context")
    void shouldPutComplexObjectIntoContext() {
      Map<String, String> complexValue = Map.of("key1", "value1", "key2", "value2");

      repository.put(CONTEXT_ID, ENTRY_NAME, complexValue);

      verify(hashOperations).put(EXPECTED_CONTEXT_KEY, ENTRY_NAME, complexValue);
    }

    @Test
    @DisplayName("should throw exception when contextId is null")
    void shouldThrowExceptionWhenContextIdIsNull() {
      assertThatThrownBy(() -> repository.put(null, ENTRY_NAME, "value"))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw exception when entryName is null")
    void shouldThrowExceptionWhenEntryNameIsNull() {
      assertThatThrownBy(() -> repository.put(CONTEXT_ID, null, "value"))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("Get Operations")
  class GetOperations {

    @Test
    @DisplayName("should get entry from context")
    void shouldGetEntryFromContext() {
      String expectedValue = "test-value";
      when(hashOperations.get(EXPECTED_CONTEXT_KEY, ENTRY_NAME)).thenReturn(expectedValue);
      when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(true);

      Optional<String> result = repository.get(CONTEXT_ID, ENTRY_NAME);

      assertThat(result).isPresent().contains(expectedValue);
      verify(hashOperations).get(EXPECTED_CONTEXT_KEY, ENTRY_NAME);
    }

    @Test
    @DisplayName("should return empty when entry does not exist")
    void shouldReturnEmptyWhenEntryDoesNotExist() {
      when(hashOperations.get(EXPECTED_CONTEXT_KEY, ENTRY_NAME)).thenReturn(null);
      when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(true);

      Optional<String> result = repository.get(CONTEXT_ID, ENTRY_NAME);

      assertThat(result).isEmpty();
      verify(hashOperations).get(EXPECTED_CONTEXT_KEY, ENTRY_NAME);
    }

    @Test
    @DisplayName("should get all entries from context")
    void shouldGetAllEntriesFromContext() {
      Map<Object, Object> expectedEntries = Map.of(
          "entry1", "value1",
          "entry2", "value2"
      );
      when(hashOperations.entries(EXPECTED_CONTEXT_KEY)).thenReturn(expectedEntries);
      when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(true);

      Map<String, Object> result = repository.getAll(CONTEXT_ID);

      assertThat(result).isEqualTo(expectedEntries);
      verify(hashOperations).entries(EXPECTED_CONTEXT_KEY);
    }

    @Test
    @DisplayName("should return empty map when context has no entries")
    void shouldReturnEmptyMapWhenContextHasNoEntries() {
      Map<Object, Object> emptyMap = Collections.emptyMap();
      when(hashOperations.entries(EXPECTED_CONTEXT_KEY)).thenReturn(emptyMap);
      when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(true);

      Map<String, Object> result = repository.getAll(CONTEXT_ID);

      assertThat(result).isEmpty();
      verify(hashOperations).entries(EXPECTED_CONTEXT_KEY);
    }

    @Test
    @DisplayName("should throw exception when contextId is null in get")
    void shouldThrowExceptionWhenContextIdIsNullInGet() {
      assertThatThrownBy(() -> repository.get(null, ENTRY_NAME))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw exception when entryName is null in get")
    void shouldThrowExceptionWhenEntryNameIsNullInGet() {
      assertThatThrownBy(() -> repository.get(CONTEXT_ID, null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("Delete Operations")
  class DeleteOperations {

    @Test
    @DisplayName("should delete entry from context and return true")
    void shouldDeleteEntryFromContextAndReturnTrue() {
      when(hashOperations.delete(EXPECTED_CONTEXT_KEY, ENTRY_NAME)).thenReturn(1L);
      when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(true);

      boolean result = repository.delete(CONTEXT_ID, ENTRY_NAME);

      assertThat(result).isTrue();
      verify(hashOperations).delete(EXPECTED_CONTEXT_KEY, ENTRY_NAME);
    }

    @Test
    @DisplayName("should return false when entry does not exist")
    void shouldReturnFalseWhenEntryDoesNotExist() {
      when(hashOperations.delete(EXPECTED_CONTEXT_KEY, ENTRY_NAME)).thenReturn(0L);
      when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(true);

      boolean result = repository.delete(CONTEXT_ID, ENTRY_NAME);

      assertThat(result).isFalse();
      verify(hashOperations).delete(EXPECTED_CONTEXT_KEY, ENTRY_NAME);
    }

    @Test
    @DisplayName("should delete entire context and return true")
    void shouldDeleteEntireContextAndReturnTrue() {
      when(redisTemplate.delete(EXPECTED_CONTEXT_KEY)).thenReturn(true);

      boolean result = repository.deleteAll(CONTEXT_ID);

      assertThat(result).isTrue();
      verify(redisTemplate).delete(EXPECTED_CONTEXT_KEY);
    }

    @Test
    @DisplayName("should return false when context does not exist for deleteAll")
    void shouldReturnFalseWhenContextDoesNotExistForDeleteAll() {
      when(redisTemplate.delete(EXPECTED_CONTEXT_KEY)).thenReturn(false);

      boolean result = repository.deleteAll(CONTEXT_ID);

      assertThat(result).isFalse();
      verify(redisTemplate).delete(EXPECTED_CONTEXT_KEY);
    }

    @Test
    @DisplayName("should handle null return from Redis delete")
    void shouldHandleNullReturnFromRedisDelete() {
      when(redisTemplate.delete(EXPECTED_CONTEXT_KEY)).thenReturn(null);

      boolean result = repository.deleteAll(CONTEXT_ID);

      assertThat(result).isFalse();
      verify(redisTemplate).delete(EXPECTED_CONTEXT_KEY);
    }

    @Test
    @DisplayName("should throw exception when contextId is null in delete")
    void shouldThrowExceptionWhenContextIdIsNullInDelete() {
      assertThatThrownBy(() -> repository.delete(null, ENTRY_NAME))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw exception when entryName is null in delete")
    void shouldThrowExceptionWhenEntryNameIsNullInDelete() {
      assertThatThrownBy(() -> repository.delete(CONTEXT_ID, null))
          .isInstanceOf(NullPointerException.class);
    }
  }

  @Nested
  @DisplayName("Exists Operations")
  class ExistsOperations {

    @Test
    @DisplayName("should return true when context exists")
    void shouldReturnTrueWhenContextExists() {
      when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(true);

      boolean result = repository.exists(CONTEXT_ID);

      assertThat(result).isTrue();
      verify(redisTemplate).hasKey(EXPECTED_CONTEXT_KEY);
    }

    @Test
    @DisplayName("should return false when context does not exist")
    void shouldReturnFalseWhenContextDoesNotExist() {
      when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(false);

      boolean result = repository.exists(CONTEXT_ID);

      assertThat(result).isFalse();
      verify(redisTemplate).hasKey(EXPECTED_CONTEXT_KEY);
    }

    @Test
    @DisplayName("should handle null return from Redis hasKey for context")
    void shouldHandleNullReturnFromRedisHasKeyForContext() {
      when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(null);

      boolean result = repository.exists(CONTEXT_ID);

      assertThat(result).isFalse();
      verify(redisTemplate).hasKey(EXPECTED_CONTEXT_KEY);
    }

    @Test
    @DisplayName("should return true when entry exists in context")
    void shouldReturnTrueWhenEntryExistsInContext() {
      when(hashOperations.hasKey(EXPECTED_CONTEXT_KEY, ENTRY_NAME)).thenReturn(true);
      when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(true);

      boolean result = repository.exists(CONTEXT_ID, ENTRY_NAME);

      assertThat(result).isTrue();
      verify(hashOperations).hasKey(EXPECTED_CONTEXT_KEY, ENTRY_NAME);
    }

    @Test
    @DisplayName("should return false when entry does not exist in context")
    void shouldReturnFalseWhenEntryDoesNotExistInContext() {
      when(hashOperations.hasKey(EXPECTED_CONTEXT_KEY, ENTRY_NAME)).thenReturn(false);
      when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(true);
      boolean result = repository.exists(CONTEXT_ID, ENTRY_NAME);

      assertThat(result).isFalse();
      verify(hashOperations).hasKey(EXPECTED_CONTEXT_KEY, ENTRY_NAME);
    }

    @Test
    @DisplayName("should handle null return from Redis hasKey for entry")
    void shouldHandleNullReturnFromRedisHasKeyForEntry() {
      when(hashOperations.hasKey(EXPECTED_CONTEXT_KEY, ENTRY_NAME)).thenReturn(null);
      when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(true);
      boolean result = repository.exists(CONTEXT_ID, ENTRY_NAME);

      assertThat(result).isFalse();
      verify(hashOperations).hasKey(EXPECTED_CONTEXT_KEY, ENTRY_NAME);
    }

    @Test
    @DisplayName("should throw exception when contextId is null in exists")
    void shouldThrowExceptionWhenContextIdIsNullInExists() {
      assertThatThrownBy(() -> repository.exists(null))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw exception when contextId is null in exists with entry")
    void shouldThrowExceptionWhenContextIdIsNullInExistsWithEntry() {
      assertThatThrownBy(() -> repository.exists(null, ENTRY_NAME))
          .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("should throw exception when entryName is null in exists")
    void shouldThrowExceptionWhenEntryNameIsNullInExists() {
      assertThatThrownBy(() -> repository.exists(CONTEXT_ID, null))
          .isInstanceOf(NullPointerException.class);
    }
  }


  @Nested
  @DisplayName("Key Building")
  class KeyBuilding {

    @Test
    @DisplayName("should build correct context key format")
    void shouldBuildCorrectContextKeyFormat() {
      String customKeyspace = "custom-keyspace";
      String customSuffix = "custom-suffix";
      String customContextId = "custom-context-id";
      String expectedKey = "custom-keyspace:application:custom-context-id:context:custom-suffix";

      RedisApplicationContextRepository customRepository =
          new RedisApplicationContextRepository(redisTemplate, customKeyspace, customSuffix);

      customRepository.put(customContextId, ENTRY_NAME, "value");

      verify(hashOperations).put(expectedKey, ENTRY_NAME, "value");
    }

    @Test
    @DisplayName("should handle special characters in context key components")
    void shouldHandleSpecialCharactersInContextKeyComponents() {
      String specialKeyspace = "test:keyspace-with_special.chars";
      String specialSuffix = "test:suffix-with_special.chars";
      String specialContextId = "test:context-id_with.special-chars";
      String expectedKey = "test:keyspace-with_special.chars:application:test:context-id_with" +
          ".special-chars:context:test:suffix-with_special.chars";

      RedisApplicationContextRepository specialRepository =
          new RedisApplicationContextRepository(redisTemplate, specialKeyspace, specialSuffix);

      specialRepository.put(specialContextId, ENTRY_NAME, "value");

      verify(hashOperations).put(expectedKey, ENTRY_NAME, "value");
    }
  }

  @Nested
  @DisplayName("Edge Cases and Error Handling")
  class EdgeCasesAndErrorHandling {

    @Test
    @DisplayName("should handle empty strings for contextId")
    void shouldHandleEmptyStringsForContextId() {
      String emptyContextId = "";
      String expectedKey = "test-keyspace:application::context:test-suffix";

      repository.put(emptyContextId, ENTRY_NAME, "value");

      verify(hashOperations).put(expectedKey, ENTRY_NAME, "value");
    }

    @Test
    @DisplayName("should handle empty strings for entryName")
    void shouldHandleEmptyStringsForEntryName() {
      String emptyEntryName = "";

      repository.put(CONTEXT_ID, emptyEntryName, "value");

      verify(hashOperations).put(EXPECTED_CONTEXT_KEY, emptyEntryName, "value");
    }

    @Test
    @DisplayName("should handle very long context keys")
    void shouldHandleVeryLongContextKeys() {
      String longContextId = "a".repeat(100);
      String expectedKey = String.format("test-keyspace:application:%s:context:test-suffix",
          longContextId);

      repository.put(longContextId, ENTRY_NAME, "value");

      verify(hashOperations).put(expectedKey, ENTRY_NAME, "value");
    }


    @Nested
    @DisplayName("Integration Scenarios")
    class IntegrationScenarios {

      @Test
      @DisplayName("should perform complete CRUD lifecycle")
      void shouldPerformCompleteCrudLifecycle() {
        String value = "test-value";
        Map<Object, Object> contextEntries = Map.of(ENTRY_NAME, value);

        // Setup mocks for the lifecycle
        when(hashOperations.get(EXPECTED_CONTEXT_KEY, ENTRY_NAME)).thenReturn(value);
        when(hashOperations.entries(EXPECTED_CONTEXT_KEY)).thenReturn(contextEntries);
        when(hashOperations.hasKey(EXPECTED_CONTEXT_KEY, ENTRY_NAME)).thenReturn(true);
        when(redisTemplate.hasKey(EXPECTED_CONTEXT_KEY)).thenReturn(true);
        when(hashOperations.delete(EXPECTED_CONTEXT_KEY, ENTRY_NAME)).thenReturn(1L);

        // Create
        repository.put(CONTEXT_ID, ENTRY_NAME, value);

        // Read single entry
        Optional<String> getResult = repository.get(CONTEXT_ID, ENTRY_NAME);
        assertThat(getResult).isPresent().contains(value);

        // Read all entries
        Map<String, Object> getAllResult = repository.getAll(CONTEXT_ID);
        assertThat(getAllResult).containsEntry(ENTRY_NAME, value);

        // Check existence
        assertThat(repository.exists(CONTEXT_ID)).isTrue();
        assertThat(repository.exists(CONTEXT_ID, ENTRY_NAME)).isTrue();

        // Delete single entry
        boolean deleteResult = repository.delete(CONTEXT_ID, ENTRY_NAME);
        assertThat(deleteResult).isTrue();

        // Verify all operations were called
        verify(hashOperations).put(EXPECTED_CONTEXT_KEY, ENTRY_NAME, value);
        verify(hashOperations).get(EXPECTED_CONTEXT_KEY, ENTRY_NAME);
        verify(hashOperations).entries(EXPECTED_CONTEXT_KEY);
        verify(hashOperations).hasKey(EXPECTED_CONTEXT_KEY, ENTRY_NAME);
        verify(redisTemplate, atLeastOnce()).hasKey(EXPECTED_CONTEXT_KEY);
        verify(hashOperations).delete(EXPECTED_CONTEXT_KEY, ENTRY_NAME);
      }

      @Test
      @DisplayName("should handle concurrent operations scenario")
      void shouldHandleConcurrentOperationsScenario() {

        // Multiple puts with expiration
        repository.put(CONTEXT_ID, "entry1", "value1");
        repository.put(CONTEXT_ID, "entry2", "value2");

        // Verify both operations called expire
        verify(hashOperations).put(EXPECTED_CONTEXT_KEY, "entry1", "value1");
        verify(hashOperations).put(EXPECTED_CONTEXT_KEY, "entry2", "value2");
      }

      @Test
      @DisplayName("should handle mixed type values")
      void shouldHandleMixedTypeValues() {
        Object stringValue = "string-value";
        Object integerValue = 42;
        Object mapValue = Map.of("nested", "value");

        repository.put(CONTEXT_ID, "string-entry", stringValue);
        repository.put(CONTEXT_ID, "integer-entry", integerValue);
        repository.put(CONTEXT_ID, "map-entry", mapValue);

        verify(hashOperations).put(EXPECTED_CONTEXT_KEY, "string-entry", stringValue);
        verify(hashOperations).put(EXPECTED_CONTEXT_KEY, "integer-entry", integerValue);
        verify(hashOperations).put(EXPECTED_CONTEXT_KEY, "map-entry", mapValue);
      }
    }
  }
}
