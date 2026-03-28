/*
 * Copyright 2025 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.digital.data.platform.storage.form.service;

import com.epam.digital.data.platform.storage.form.BaseRedisIT;
import com.epam.digital.data.platform.storage.form.model.ApplicationStepInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ApplicationContextService Integration Tests")
class ApplicationContextServiceIT extends BaseRedisIT {

  private String contextId;
  private String entryName;
  private String entryValue;
  private String varsKey;
  private String stepsKey;

  @Override
  @BeforeEach
  public void setUp() {
    contextId = "test-context-id";
    entryName = "test-entry";
    entryValue = "test-value";
    varsKey = "test-space:application:" + contextId + ":context:vars";
    stepsKey = "test-space:application:" + contextId + ":context:steps";

    // Clear Redis before each test
    Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection().serverCommands().flushAll();
  }

  @Nested
  @DisplayName("Put Operations Tests")
  class PutOperationsTests {

    @Test
    @DisplayName("Should put entry without expiration")
    void shouldPutEntryWithoutExpiration() {
      // when
      applicationContextService.vars().put(contextId, entryName, entryValue);

      // then
      String storedValue = (String) redisTemplate.opsForHash().get(varsKey, entryName);
      assertNotNull(storedValue);
      assertEquals(entryValue, storedValue);
    }

    @Test
    @DisplayName("Should put null value")
    void shouldPutNullValue() {
      // when
      assertDoesNotThrow(() -> applicationContextService.vars().put(contextId, entryName, null));

      // then
      var storedValue = (String) redisTemplate.opsForHash().get(varsKey, entryName);
      assertNull(storedValue);
    }

    @Test
    @DisplayName("Should put complex object")
    void shouldPutComplexObject() {
      // given
      Map<String, Object> complexObject = Map.of("key1", "value1", "key2", 42);

      // when
      applicationContextService.vars().put(contextId, entryName, complexObject);

      // then
      Object storedValue = redisTemplate.opsForHash().get(varsKey, entryName);
      assertEquals(complexObject, storedValue);
    }

    @Test
    @DisplayName("Should put entry in steps context")
    void shouldPutEntryInStepsContext() {
      // given - empty Redis
      var id = UUID.randomUUID().toString();
      var step = new ApplicationStepInfo(
          id, ApplicationStepInfo.StepType.FORM_SUBMISSION, ApplicationStepInfo.Status.PENDING, null, Map.of(
          "some_key", "some_value"));

      // when
      applicationContextService.steps().put(contextId, id, step);

      // then
      assertTrue(redisTemplate.hasKey(stepsKey));
      Object storedValue = redisTemplate.opsForHash().get(stepsKey, id);
      assertNotNull(storedValue);
      assertEquals(step, storedValue);
    }
  }

  @Nested
  @DisplayName("Get Operations Tests")
  class GetOperationsTests {

    @Test
    @DisplayName("Should get existing entry")
    void shouldGetExistingEntry() {
      // given
      redisTemplate.opsForHash().put(varsKey, entryName, entryValue);

      // when
      Optional<String> result = applicationContextService.vars().get(contextId, entryName);

      // then
      assertTrue(result.isPresent());
      assertEquals(entryValue, result.get());
    }

    @Test
    @DisplayName("Should return empty for non-existent entry")
    void shouldReturnEmptyForNonExistentEntry() {
      // given
      redisTemplate.opsForHash().put(varsKey, "other-entry", "value");

      // when
      Optional<String> result = applicationContextService.vars().get(contextId, entryName);

      // then
      assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should get with default value when entry exists")
    void shouldGetWithDefaultValueWhenEntryExists() {
      // given
      String defaultValue = "default-value";
      redisTemplate.opsForHash().put(varsKey, entryName, entryValue);

      // when
      String result = applicationContextService.vars().getOrDefault(contextId, entryName, defaultValue);

      // then
      assertEquals(entryValue, result);
      assertNotEquals(defaultValue, result);
    }

    @Test
    @DisplayName("Should return default value when entry does not exist")
    void shouldReturnDefaultValueWhenEntryDoesNotExist() {
      // given
      String defaultValue = "default-value";
      redisTemplate.opsForHash().put(varsKey, "other-entry", "value");

      // when
      String result = applicationContextService.vars().getOrDefault(contextId, entryName, defaultValue);

      // then
      assertEquals(defaultValue, result);
    }

    @Test
    @DisplayName("Should return default value when context does not exist")
    void shouldReturnDefaultValueWhenContextDoesNotExist() {
      // given
      String defaultValue = "default-value";

      // when
      String result = applicationContextService.vars().getOrDefault(contextId, entryName, defaultValue);

      // then
      assertEquals(defaultValue, result);
    }

    @Test
    @DisplayName("Should get all entries from context")
    void shouldGetAllEntriesFromContext() {
      // given
      redisTemplate.opsForHash().put(varsKey, "entry1", Map.of("entryKey1", "value1"));
      redisTemplate.opsForHash().put(varsKey, "entry2", "value2");

      // when
      Map<String, Object> result = applicationContextService.vars().getAll(contextId);

      // then
      Map<String, Object> expectedMap = Map.of("entry1", Map.of("entryKey1", "value1"), "entry2", "value2");
      assertEquals(expectedMap, result);
    }

  }

  @Nested
  @DisplayName("Delete Operations Tests")
  class DeleteOperationsTests {

    @Test
    @DisplayName("Should delete existing entry")
    void shouldDeleteExistingEntry() {
      // given
      redisTemplate.opsForHash().put(varsKey, entryName, entryValue);

      // when
      boolean result = applicationContextService.vars().delete(contextId, entryName);

      // then
      assertTrue(result);
      assertFalse(redisTemplate.opsForHash().hasKey(varsKey, entryName));
    }

    @Test
    @DisplayName("Should return false when deleting non-existent entry")
    void shouldReturnFalseWhenDeletingNonExistentEntry() {
      // given
      redisTemplate.opsForHash().put(varsKey, "other-entry", "value");

      // when
      boolean result = applicationContextService.vars().delete(contextId, entryName);

      // then
      assertFalse(result);
    }

    @Test
    @DisplayName("Should delete entire context")
    void shouldDeleteEntireContext() {
      // given
      redisTemplate.opsForHash().put(varsKey, "entry1", "value1");
      redisTemplate.opsForHash().put(varsKey, "entry2", "value2");

      // when
      boolean result = applicationContextService.vars().deleteContext(contextId);

      // then
      assertTrue(result);
      assertFalse(redisTemplate.hasKey(varsKey));
    }

    @Test
    @DisplayName("Should return false when deleting non-existent context")
    void shouldReturnFalseWhenDeletingNonExistentContext() {
      // given - no context in Redis

      // when
      boolean result = applicationContextService.vars().deleteContext(contextId);

      // then
      assertFalse(result);
    }
  }

  @Nested
  @DisplayName("Cross-Service Tests")
  class CrossServiceTests {

    @Test
    @DisplayName("Should work independently with vars and steps services")
    void shouldWorkIndependentlyWithVarsAndStepsServices() {
      // given
      String varsEntry = "vars-entry";
      String stepsEntry = "steps-entry";

      // when
      applicationContextService.vars().put(contextId, varsEntry, "vars-value");
      applicationContextService.steps().put(contextId, stepsEntry, "steps-value");

      assertTrue(applicationContextService.vars().exists(contextId, varsEntry));
      assertTrue(applicationContextService.steps().exists(contextId, stepsEntry));

      // Verify separation - vars shouldn't see steps entries and vice versa
      assertFalse(applicationContextService.vars().exists(contextId, stepsEntry));
      assertFalse(applicationContextService.steps().exists(contextId, varsEntry));

      // Verify different Redis keys are used
      assertTrue(redisTemplate.hasKey(varsKey));
      assertTrue(redisTemplate.hasKey(stepsKey));
      assertNotEquals(varsKey, stepsKey);
    }

    @Test
    @DisplayName("Should handle different data types across services")
    void shouldHandleDifferentDataTypesAcrossServices() {
      // given
      Map<String, Object> varsData = Map.of("key", "value", "number", 42);
      String stepsData = "simple-string";

      // when
      applicationContextService.vars().put(contextId, "complex-data", varsData);
      applicationContextService.steps().put(contextId, "simple-data", stepsData);

      // then
      Optional<Map<String, Object>> retrievedVarsData = applicationContextService.vars().get(contextId, "complex-data");
      Optional<String> retrievedStepsData = applicationContextService.steps().get(contextId, "simple-data");

      assertTrue(retrievedVarsData.isPresent());
      assertTrue(retrievedStepsData.isPresent());
      assertEquals(stepsData, retrievedStepsData.get());

      // Verify correct Redis keys
      assertTrue(redisTemplate.opsForHash().hasKey(varsKey, "complex-data"));
      assertTrue(redisTemplate.opsForHash().hasKey(stepsKey, "simple-data"));
    }

  }

  @Nested
  @DisplayName("End-to-End Integration Tests")
  class EndToEndIntegrationTests {
    @Test
    @DisplayName("Should handle concurrent operations on different context types")
    void shouldHandleConcurrentOperationsOnDifferentContextTypes() {
      // given
      String contextId1 = "context-1";
      String contextId2 = "context-2";
      String varsKey1 = "test-space:application:" + contextId1 + ":context:vars";
      String stepsKey1 = "test-space:application:" + contextId1 + ":context:steps";
      String varsKey2 = "test-space:application:" + contextId2 + ":context:vars";

      // when - create multiple contexts and types
      applicationContextService.vars().put(contextId1, "vars-entry", "vars-value");
      applicationContextService.steps().put(contextId1, "steps-entry", "steps-value");
      applicationContextService.vars().put(contextId2, "another-entry", "another-value");

      // then - verify all contexts and types exist independently
      assertTrue(redisTemplate.hasKey(varsKey1));
      assertTrue(redisTemplate.hasKey(stepsKey1));
      assertTrue(redisTemplate.hasKey(varsKey2));

      // Verify data integrity across contexts
      assertTrue(applicationContextService.vars().exists(contextId1, "vars-entry"));
      assertTrue(applicationContextService.steps().exists(contextId1, "steps-entry"));
      assertTrue(applicationContextService.vars().exists(contextId2, "another-entry"));

      // Verify isolation
      assertFalse(applicationContextService.vars().exists(contextId1, "steps-entry"));
      assertFalse(applicationContextService.steps().exists(contextId1, "vars-entry"));
      assertFalse(applicationContextService.vars().exists(contextId2, "vars-entry"));
    }
  }
}