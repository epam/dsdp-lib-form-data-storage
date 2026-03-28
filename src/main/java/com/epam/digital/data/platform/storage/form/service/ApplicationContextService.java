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

import com.epam.digital.data.platform.storage.form.exception.ContextNotFoundException;
import com.epam.digital.data.platform.storage.form.model.ApplicationContextType;
import com.epam.digital.data.platform.storage.form.repository.ApplicationContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * High-level service for managing application context operations.
 * Provides convenient methods with validation and error handling.
 */
@Slf4j
@Service
public class ApplicationContextService {

  private final Map<ApplicationContextType, Service> services;

  public ApplicationContextService(@NonNull Map<ApplicationContextType, ApplicationContextRepository> repositories) {
    this.services = new EnumMap<>(ApplicationContextType.class);
    repositories.forEach((type, repo) ->
        services.put(type, new Service(repo)));
  }

  public Service vars() {
    return services.get(ApplicationContextType.VARS);
  }

  public Service steps() {
    return services.get(ApplicationContextType.STEPS);
  }

  @RequiredArgsConstructor
  public static class Service {
    private final ApplicationContextRepository repository;

    /**
     * Puts an entry in the application context, creating the context if it doesn't exist.
     *
     * @param contextId the unique identifier of the context
     * @param entryName the name of the entry to store
     * @param value     the value to store (will be serialized to JSON)
     * @param <T>       the type of the value
     */
    public <T> void put(@NonNull String contextId, @NonNull String entryName, @Nullable T value) {
      log.debug("Putting entry '{}' in context '{}'", entryName, contextId);
      repository.put(contextId, entryName, value);
    }

    /**
     * Retrieves an entry from the application context.
     *
     * @param contextId the unique identifier of the context
     * @param entryName the name of the entry to retrieve
     * @param <T>       the type of the value
     * @return an Optional containing the entry value if it exists, empty otherwise
     */
    public <T> Optional<T> get(@NonNull String contextId, @NonNull String entryName) {
      log.debug("Getting entry '{}' from context '{}'", entryName, contextId);
      return repository.get(contextId, entryName);
    }

    /**
     * Retrieves an entry from the application context with a default value.
     *
     * @param contextId    the unique identifier of the context
     * @param entryName    the name of the entry to retrieve
     * @param defaultValue the default value to return if entry doesn't exist
     * @param <T>          the type of the value
     * @return the entry value or the default value if not found
     */
    public <T> T getOrDefault(@NonNull String contextId, @NonNull String entryName, @Nullable T defaultValue) {
      return repository.<T>get(contextId, entryName).orElse(defaultValue);
    }

    /**
     * Retrieves all entries from the application context.
     *
     * @param contextId the unique identifier of the context
     * @return a map of entry names to their JSON string representations
     */
    public Map<String, Object> getAll(@NonNull String contextId) {
      log.debug("Getting all entries from context '{}'", contextId);
      return repository.getAll(contextId);
    }

    /**
     * Deletes a specific entry from the application context.
     *
     * @param contextId the unique identifier of the context
     * @param entryName the name of the entry to delete
     * @return true if the entry was deleted, false if it didn't exist
     */
    public boolean delete(@NonNull String contextId, @NonNull String entryName) {
      log.debug("Deleting entry '{}' from context '{}'", entryName, contextId);
      return repository.delete(contextId, entryName);
    }

    /**
     * Deletes the entire application context with all its entries.
     *
     * @param contextId the unique identifier of the context to delete
     * @return true if the context was deleted, false if it didn't exist
     */
    public boolean deleteContext(@NonNull String contextId) {
      log.debug("Deleting context '{}'", contextId);
      return repository.deleteAll(contextId);
    }
    /**
     * Checks if a specific entry exists in the application context.
     *
     * @param contextId the unique identifier of the context
     * @param entryName the name of the entry to check
     * @return true if the entry exists, false otherwise
     * @throws ContextNotFoundException if the context doesn't exist
     */
    public boolean exists(@NonNull String contextId, @NonNull String entryName) {
      return repository.exists(contextId, entryName);
    }
  }
}
