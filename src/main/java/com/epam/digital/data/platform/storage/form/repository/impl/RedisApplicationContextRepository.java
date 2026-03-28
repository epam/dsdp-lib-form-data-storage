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

import com.epam.digital.data.platform.storage.form.repository.ApplicationContextRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Redis implementation of the ApplicationContextRepository.
 * Stores context entries as JSON strings in Redis using hash data structures.
 * Each context is stored as a Redis hash with the key pattern:
 * "{keyspace}:application:{contextId}:context:{redisKeySuffix}".
 */
@Slf4j
@RequiredArgsConstructor
public class RedisApplicationContextRepository implements ApplicationContextRepository {

  private static final String CONTEXT_ID_IS_NULL = "Context id is null";
  private static final String ENTRY_NAME_IS_NULL = "Entry name is null";

  private static final String APPLICATION_KEY_FORMAT = "%s:application:%s:context:%s";

  private final RedisTemplate<String, Object> redisTemplate;
  private final String keyspace;
  private final String redisKeySuffix;

  @Override
  public <T> void put(@NonNull String contextId, @NonNull String entryName, @Nullable T value) {
    Objects.requireNonNull(contextId, CONTEXT_ID_IS_NULL);
    Objects.requireNonNull(entryName, ENTRY_NAME_IS_NULL);

    var contextKey = buildContextKey(contextId);

    redisTemplate.opsForHash().put(contextKey, entryName, value);

    log.debug("Set entry '{}' in context '{}' with value: {}", entryName, contextId, value);
  }

  @Override
  public <T> Optional<T> get(@NonNull String contextId, @NonNull String entryName) {
    Objects.requireNonNull(contextId, CONTEXT_ID_IS_NULL);
    Objects.requireNonNull(entryName, ENTRY_NAME_IS_NULL);
    if(!exists(contextId)){
      return Optional.empty();
    }
    var contextKey = buildContextKey(contextId);
    var rawValue = redisTemplate.opsForHash().get(contextKey, entryName);
    return Optional.ofNullable((T) rawValue);
  }

  @Override
  public Map<String, Object> getAll(@NonNull String contextId) {
    Objects.requireNonNull(contextId, CONTEXT_ID_IS_NULL);
    if(!exists(contextId)){
      return new HashMap<>();
    }
    var contextKey = buildContextKey(contextId);
    var result = redisTemplate.<String, Object>opsForHash().entries(contextKey);

    log.debug("Retrieved {} entries from context '{}'", result.size(), contextId);
    return result;
  }

  @Override
  public boolean delete(@NonNull String contextId, @NonNull String entryName) {
    Objects.requireNonNull(contextId, CONTEXT_ID_IS_NULL);
    Objects.requireNonNull(entryName, ENTRY_NAME_IS_NULL);
    if(!exists(contextId)){
      return false;
    }
    var contextKey = buildContextKey(contextId);
    var deletedCount = redisTemplate.opsForHash().delete(contextKey, entryName);

    var deleted = deletedCount > 0;
    log.debug("Entry '{}' in context '{}' deleted: {}", entryName, contextId, deleted);
    return deleted;
  }

  @Override
  public boolean deleteAll(@NonNull String contextId) {
    Objects.requireNonNull(contextId, CONTEXT_ID_IS_NULL);

    var contextKey = buildContextKey(contextId);
    var deleted = redisTemplate.delete(contextKey);

    var result = Boolean.TRUE.equals(deleted);
    log.debug("Context '{}' deleted: {}", contextId, result);
    return result;
  }

  public boolean exists(@NonNull String contextId) {
    Objects.requireNonNull(contextId, CONTEXT_ID_IS_NULL);

    var contextKey = buildContextKey(contextId);
    var exists = redisTemplate.hasKey(contextKey);

    var result = Boolean.TRUE.equals(exists);
    log.debug("Context '{}' exists: {}", contextId, result);
    return result;
  }
  @Override
  public boolean exists(@NonNull String contextId, @NonNull String entryName) {
    Objects.requireNonNull(contextId, CONTEXT_ID_IS_NULL);
    Objects.requireNonNull(entryName, ENTRY_NAME_IS_NULL);
    if(!exists(contextId)){
      return false;
    }
    var contextKey = buildContextKey(contextId);
    var exists = redisTemplate.opsForHash().hasKey(contextKey, entryName);

    var result = Boolean.TRUE.equals(exists);
    log.debug("Entry '{}' in context '{}' exists: {}", entryName, contextId, result);
    return result;
  }

  /**
   * Builds the Redis key for a context.
   *
   * @param contextId the context identifier
   * @return the Redis key in format "{keyspace}:application:{contextId}:context:{redisKeySuffix}"
   */
  private String buildContextKey(@NonNull String contextId) {
    return String.format(APPLICATION_KEY_FORMAT, keyspace, contextId, redisKeySuffix);
  }
}
