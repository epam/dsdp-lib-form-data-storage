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
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MongoVarsContextRepositoryTest {

  private static final String TOKEN = "token";

  @Mock
  private ExternalContextService service;
  @Mock
  IdmService idmService;
  private ApplicationContextRepository repository;
  ObjectMapper mapper;
  SerializationService serializationService;

  @BeforeEach
  void setUp() {
    mapper = new ObjectMapper();
    serializationService = new SerializationService(mapper);
    repository = new MongoVarsContextRepository(service, serializationService, idmService);
    when(idmService.getClientAccessToken()).thenReturn(TOKEN);
  }


  @Test
  void putShouldDelegateToServiceWithoutTtl() {
    String contextId = "ctx-1";
    String entryName = "var-A";
    Map<String, Object> value = Map.of("key", "val");

    repository.put(contextId, entryName, value);

    verify(service, times(1)).putVar(eq(contextId), eq(entryName), anyString(), eq(TOKEN));
    verifyNoMoreInteractions(service);
  }

  @Test
  void getShouldReturnEmptyWhenServiceReturnsEmpty() {
    String contextId = "ctx-2";
    String entryName = "var-B";

    when(service.getVar(contextId, entryName, TOKEN)).thenReturn(Optional.empty());

    Optional<Object> result = repository.get(contextId, entryName);

    assertTrue(result.isEmpty());
    verify(service, times(1)).getVar(contextId, entryName, TOKEN);
    verifyNoMoreInteractions(service);
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

    when(service.getAllVars(contextId, TOKEN)).thenReturn(steps);

    Map<String, Object> result = repository.getAll(contextId);

    assertEquals(expected, result);
    verify(service, times(1)).getAllVars(contextId, TOKEN);
    verifyNoMoreInteractions(service);
  }

  @Test
  void deleteShouldReturnTrueWhenServiceReturnsTrue() {
    String ctx = "ctx";
    String entry = "entry";

    when(service.deleteVar(ctx, entry, TOKEN)).thenReturn(Boolean.TRUE);

    boolean deleted = repository.delete(ctx, entry);

    assertTrue(deleted);
    verify(service).deleteVar(ctx, entry, TOKEN);
    verifyNoMoreInteractions(service);
  }

  @Test
  void deleteShouldReturnFalseWhenServiceReturnsFalseOrNull() {
    String ctx1 = "ctx1";
    String entry1 = "entry1";
    String ctx2 = "ctx2";
    String entry2 = "entry2";

    when(service.deleteVar(ctx1, entry1, TOKEN)).thenReturn(Boolean.FALSE);
    when(service.deleteVar(ctx2, entry2, TOKEN)).thenReturn(null);

    assertFalse(repository.delete(ctx1, entry1));
    assertFalse(repository.delete(ctx2, entry2));

    verify(service).deleteVar(ctx1, entry1, TOKEN);
    verify(service).deleteVar(ctx2, entry2, TOKEN);
    verifyNoMoreInteractions(service);
  }

  @Test
  void deleteAllShouldReturnTrueWhenServiceReturnsTrue() {
    String ctx = "ctx";

    when(service.deleteVarsContext(ctx, TOKEN)).thenReturn(Boolean.TRUE);

    boolean deletedAll = repository.deleteAll(ctx);

    assertTrue(deletedAll);
    verify(service).deleteVarsContext(ctx, TOKEN);
    verifyNoMoreInteractions(service);
  }

  @Test
  void deleteAllShouldReturnFalseWhenServiceReturnsFalseOrNull() {
    String ctx1 = "ctx1";
    String ctx2 = "ctx2";

    when(service.deleteVarsContext(ctx1, TOKEN)).thenReturn(Boolean.FALSE);
    when(service.deleteVarsContext(ctx2, TOKEN)).thenReturn(null);

    assertFalse(repository.deleteAll(ctx1));
    assertFalse(repository.deleteAll(ctx2));

    verify(service).deleteVarsContext(ctx1, TOKEN);
    verify(service).deleteVarsContext(ctx2, TOKEN);
    verifyNoMoreInteractions(service);
  }
}
