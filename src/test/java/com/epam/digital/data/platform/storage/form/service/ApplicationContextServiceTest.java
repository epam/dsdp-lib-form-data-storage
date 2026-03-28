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

import com.epam.digital.data.platform.storage.form.model.ApplicationContextType;
import com.epam.digital.data.platform.storage.form.repository.ApplicationContextRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApplicationContextService Tests")
class ApplicationContextServiceTest {

  @Mock
  private ApplicationContextRepository varsRepository;

  @Mock
  private ApplicationContextRepository stepsRepository;

  private ApplicationContextService applicationContextService;

  @BeforeEach
  void setUp() {
    Map<ApplicationContextType, ApplicationContextRepository> repositories = Map.of(
        ApplicationContextType.VARS, varsRepository,
        ApplicationContextType.STEPS, stepsRepository
    );
    applicationContextService = new ApplicationContextService(repositories);
  }

  @Nested
  @DisplayName("Main Service Tests")
  class MainServiceTests {

    @Test
    @DisplayName("Should return vars service when vars() is called")
    void shouldReturnVarsService() {
      // when
      ApplicationContextService.Service varsService = applicationContextService.vars();

      // then
      assertThat(varsService).isNotNull();
    }

    @Test
    @DisplayName("Should return steps service when steps() is called")
    void shouldReturnStepsService() {
      // when
      ApplicationContextService.Service stepsService = applicationContextService.steps();

      // then
      assertThat(stepsService).isNotNull();
    }

    @Test
    @DisplayName("Should create services for all repository types")
    void shouldCreateServicesForAllRepositoryTypes() {
      // when & then
      assertThat(applicationContextService.vars()).isNotNull();
      assertThat(applicationContextService.steps()).isNotNull();
    }
  }

  @Nested
  @DisplayName("Inner Service Tests")
  class InnerServiceTests {

    private ApplicationContextService.Service service;
    private static final String CONTEXT_ID = "test-context";
    private static final String ENTRY_NAME = "test-entry";
    private static final String TEST_VALUE = "test-value";

    @BeforeEach
    void setUp() {
      service = new ApplicationContextService.Service(varsRepository);
    }

    @Nested
    @DisplayName("Put Operations")
    class PutOperations {

      @Test
      @DisplayName("Should put entry without expiration")
      void shouldPutEntryWithoutExpiration() {
        // when
        service.put(CONTEXT_ID, ENTRY_NAME, TEST_VALUE);

        // then
        verify(varsRepository).put(CONTEXT_ID, ENTRY_NAME, TEST_VALUE);
      }

      @Test
      @DisplayName("Should put null value")
      void shouldPutNullValue() {
        // when
        service.put(CONTEXT_ID, ENTRY_NAME, null);

        // then
        verify(varsRepository).put(CONTEXT_ID, ENTRY_NAME, null);
      }
    }

    @Nested
    @DisplayName("Get Operations")
    class GetOperations {

      @Test
      @DisplayName("Should get entry when context exists")
      void shouldGetEntryWhenContextExists() {
        // given
        when(varsRepository.get(CONTEXT_ID, ENTRY_NAME)).thenReturn(Optional.of(TEST_VALUE));

        // when
        Optional<String> result = service.get(CONTEXT_ID, ENTRY_NAME);

        // then
        assertThat(result).contains(TEST_VALUE);
        verify(varsRepository).get(CONTEXT_ID, ENTRY_NAME);
      }


      @Test
      @DisplayName("Should return empty optional when entry does not exist")
      void shouldReturnEmptyOptionalWhenEntryDoesNotExist() {
        // given
        when(varsRepository.get(CONTEXT_ID, ENTRY_NAME)).thenReturn(Optional.empty());

        // when
        Optional<String> result = service.get(CONTEXT_ID, ENTRY_NAME);

        // then
        assertThat(result).isEmpty();
      }
    }

    @Nested
    @DisplayName("GetOrDefault Operations")
    class GetOrDefaultOperations {

      @Test
      @DisplayName("Should return entry value when context and entry exist")
      void shouldReturnEntryValueWhenExists() {
        // given
        when(varsRepository.get(CONTEXT_ID, ENTRY_NAME)).thenReturn(Optional.of(TEST_VALUE));

        // when
        String result = service.getOrDefault(CONTEXT_ID, ENTRY_NAME, "default");

        // then
        assertThat(result).isEqualTo(TEST_VALUE);
      }

      @Test
      @DisplayName("Should return default value when context does not exist")
      void shouldReturnDefaultValueWhenContextDoesNotExist() {
        // given
        String defaultValue = "default-value";

        // when
        String result = service.getOrDefault(CONTEXT_ID, ENTRY_NAME, defaultValue);

        // then
        assertThat(result).isEqualTo(defaultValue);
      }

      @Test
      @DisplayName("Should return default value when entry does not exist")
      void shouldReturnDefaultValueWhenEntryDoesNotExist() {
        // given
        when(varsRepository.get(CONTEXT_ID, ENTRY_NAME)).thenReturn(Optional.empty());
        String defaultValue = "default-value";

        // when
        String result = service.getOrDefault(CONTEXT_ID, ENTRY_NAME, defaultValue);

        // then
        assertThat(result).isEqualTo(defaultValue);
      }

      @Test
      @DisplayName("Should return null default value")
      void shouldReturnNullDefaultValue() {

        // when
        String result = service.getOrDefault(CONTEXT_ID, ENTRY_NAME, null);

        // then
        assertThat(result).isNull();
      }
    }

    @Nested
    @DisplayName("GetAll Operations")
    class GetAllOperations {

      @Test
      @DisplayName("Should get all entries when context exists")
      void shouldGetAllEntriesWhenContextExists() {
        // given
        Map<String, Object> expectedEntries = Map.of("key1", "value1", "key2", "value2");
        when(varsRepository.getAll(CONTEXT_ID)).thenReturn(expectedEntries);

        // when
        Map<String, Object> result = service.getAll(CONTEXT_ID);

        // then
        assertThat(result).isEqualTo(expectedEntries);
        verify(varsRepository).getAll(CONTEXT_ID);
      }

    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {

      @Test
      @DisplayName("Should delete entry when context exists")
      void shouldDeleteEntryWhenContextExists() {
        when(varsRepository.delete(CONTEXT_ID, ENTRY_NAME)).thenReturn(true);

        // when
        boolean result = service.delete(CONTEXT_ID, ENTRY_NAME);

        // then
        assertThat(result).isTrue();
        verify(varsRepository).delete(CONTEXT_ID, ENTRY_NAME);
      }

      @Test
      @DisplayName("Should return false when entry does not exist")
      void shouldReturnFalseWhenEntryDoesNotExist() {
        // given
        when(varsRepository.delete(CONTEXT_ID, ENTRY_NAME)).thenReturn(false);

        // when
        boolean result = service.delete(CONTEXT_ID, ENTRY_NAME);

        // then
        assertThat(result).isFalse();
      }

      @Test
      @DisplayName("Should delete entire context")
      void shouldDeleteEntireContext() {
        // given
        when(varsRepository.deleteAll(CONTEXT_ID)).thenReturn(true);

        // when
        boolean result = service.deleteContext(CONTEXT_ID);

        // then
        assertThat(result).isTrue();
        verify(varsRepository).deleteAll(CONTEXT_ID);
      }

      @Test
      @DisplayName("Should return false when context to delete does not exist")
      void shouldReturnFalseWhenContextToDeleteDoesNotExist() {
        // given
        when(varsRepository.deleteAll(CONTEXT_ID)).thenReturn(false);

        // when
        boolean result = service.deleteContext(CONTEXT_ID);

        // then
        assertThat(result).isFalse();
      }
    }

  }
}
