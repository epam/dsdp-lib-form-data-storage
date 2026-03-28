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

package com.epam.digital.data.platform.storage.form.repository;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.Map;
import java.util.Optional;

/**
 * Repository for managing application context entries in business processes.
 * Provides operations to store, retrieve, update, and delete context entries
 * and entire contexts.
 */
public interface ApplicationContextRepository {

  /**
   * Stores or updates an entry in the application context.
   *
   * @param contextId the unique identifier of the context
   * @param entryName the name of the entry to store
   * @param value     the value to store (will be serialized to JSON)
   * @param <T>       the type of the value
   */
  <T> void put(@NonNull String contextId, @NonNull String entryName, @Nullable T value);

  /**
   * Retrieves a entry from the application context as string.
   *
   * @param contextId the unique identifier of the context
   * @param entryName the name of the entry to retrieve
   * @return an Optional containing the entry value if it exists, empty otherwise
   */
  <T> Optional<T> get(@NonNull String contextId, @NonNull String entryName);

  /**
   * Retrieves all entries from the application context.
   *
   * @param contextId the unique identifier of the context
   * @return a map of entry names to their JSON string representations
   */
  Map<String, Object> getAll(@NonNull String contextId);

  /**
   * Deletes a specific entry from the application context.
   *
   * @param contextId the unique identifier of the context
   * @param entryName the name of the entry to delete
   * @return true if the entry was deleted, false if it didn't exist
   */
  boolean delete(@NonNull String contextId, @NonNull String entryName);

  /**
   * Deletes the entire application context with all its entries.
   *
   * @param contextId the unique identifier of the context to delete
   * @return true if the context was deleted, false if it didn't exist
   */
  boolean deleteAll(@NonNull String contextId);

  /**
   * Checks if a specific entry exists in the application context.
   *
   * @param contextId the unique identifier of the context
   * @param entryName the name of the entry to check
   * @return true if the entry exists, false otherwise
   */
  boolean exists(@NonNull String contextId, @NonNull String entryName);

}
