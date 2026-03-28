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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@RequiredArgsConstructor
public class MongoVarsContextRepository implements ApplicationContextRepository {

  private final ExternalContextService service;
  private final SerializationService serializationService;
  private final IdmService idmService;

  @Override
  public <T> void put(@NonNull String contextId,
                      @NonNull String entryName,
                      @Nullable T value) {
    service.putVar(contextId, entryName, serializationService.serialize(value), idmService.getClientAccessToken());
  }

  @Override
  public <T> Optional<T> get(@NonNull String contextId,
                             @NonNull String entryName) {
    String rawString = service.getVar(contextId, entryName, idmService.getClientAccessToken()).orElse(null);
    return Optional.ofNullable((T) serializationService.deserialize(rawString));
  }

  @Override
  public Map<String, Object> getAll(@NonNull String contextId) {
    return service.getAllVars(contextId, idmService.getClientAccessToken()).entrySet().stream()
        .collect(Collectors.toMap(
            Map.Entry::getKey,
            entry -> serializationService.deserialize((String) entry.getValue())
        ));
  }

  @Override
  public boolean delete(@NonNull String contextId,
                        @NonNull String entryName) {
    return Boolean.TRUE.equals(service.deleteVar(contextId, entryName, idmService.getClientAccessToken()));
  }

  @Override
  public boolean deleteAll(@NonNull String contextId) {
    return Boolean.TRUE.equals(service.deleteVarsContext(contextId, idmService.getClientAccessToken()));
  }

  @Override
  public boolean exists(@NonNull String contextId, @NonNull String entryName){
    return get(contextId,entryName).isPresent();
  }

}
