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
package com.epam.digital.data.platform.storage.form.repository.impl;

import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.storage.form.repository.ApplicationContextRepository;
import com.epam.digital.data.platform.storage.form.service.ExternalContextService;
import com.epam.digital.data.platform.storage.form.service.SerializationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoStepsContextRepositoryTest {

  private static final String TOKEN = "token";

  @Mock
  ExternalContextService service;
  @Mock
  IdmService idmService;

  ObjectMapper mapper;

  ApplicationContextRepository repository;
  SerializationService serializationService;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper();
    serializationService = new SerializationService(mapper);
    repository = new MongoStepsContextRepository(service, serializationService, idmService);
    when(idmService.getClientAccessToken()).thenReturn(TOKEN);
  }

  @Test
  void getAllShouldDelegateToService() {
    String contextId = "ctx-3";
    String keyA = "A";
    String keyB = "B";
    String valueStringA = "string";
    String valueStringB = "stringPlain";
    String valueA = serializationService.serialize(valueStringA);
    String valueB = serializationService.serialize(valueStringB);
    Map<String, Object> steps = Map.of(keyA, valueA, keyB, valueB);
    Map<String, Object> expected = Map.of(keyA, valueStringA, keyB, valueStringB);

    when(service.getAllSteps(contextId, TOKEN)).thenReturn(steps);

    Map<String, Object> result = repository.getAll(contextId);

    assertEquals(expected, result);
    verify(service, times(1)).getAllSteps(contextId, TOKEN);
    verifyNoMoreInteractions(service);
  }

  @Test
  void deleteShouldReturnTrueWhenServiceReturnsTrue() {
    String contextId = "ctx";
    String key = "entry";
    when(service.deleteStep(contextId, key, TOKEN)).thenReturn(Boolean.TRUE);

    boolean deleted = repository.delete(contextId, key);

    assertTrue(deleted);
    verify(service).deleteStep(contextId, key, TOKEN);
    verifyNoMoreInteractions(service);
  }

  @Test
  void deleteShouldReturnFalseWhenServiceReturnsFalseOrNull() {
    String contextId1 = "ctx1";
    String key1 = "entry1";
    String contextId2 = "ctx2";
    String key2 = "entry2";
    when(service.deleteStep(contextId1, key1, TOKEN)).thenReturn(Boolean.FALSE);
    when(service.deleteStep(contextId2, key2, TOKEN)).thenReturn(null);

    assertFalse(repository.delete(contextId1, key1));
    assertFalse(repository.delete(contextId2, key2));

    verify(service).deleteStep(contextId1, key1, TOKEN);
    verify(service).deleteStep(contextId2, key2, TOKEN);
    verifyNoMoreInteractions(service);
  }

  @Test
  void deleteAllShouldReturnTrueWhenServiceReturnsTrue() {
    String contextId = "ctx";
    when(service.deleteStepsContext(contextId, TOKEN)).thenReturn(Boolean.TRUE);

    boolean deletedAll = repository.deleteAll(contextId);

    assertTrue(deletedAll);
    verify(service).deleteStepsContext(contextId, TOKEN);
    verifyNoMoreInteractions(service);
  }


  @Test
  void deleteAllShouldReturnFalseWhenServiceReturnsFalseOrNull() {
    String ctx1 = "ctx1";
    String ctx2 = "ctx2";

    when(service.deleteStepsContext(ctx1, TOKEN)).thenReturn(Boolean.FALSE);
    when(service.deleteStepsContext(ctx2, TOKEN)).thenReturn(null);

    assertFalse(repository.deleteAll(ctx1));
    assertFalse(repository.deleteAll(ctx2));

    verify(service).deleteStepsContext(ctx1, TOKEN);
    verify(service).deleteStepsContext(ctx2, TOKEN);
    verifyNoMoreInteractions(service);
  }

}

