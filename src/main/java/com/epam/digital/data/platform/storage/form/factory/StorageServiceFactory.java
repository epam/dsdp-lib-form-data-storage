/*
 * Copyright 2023-2025 EPAM Systems.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.epam.digital.data.platform.storage.form.factory;

import com.epam.digital.data.platform.integration.ceph.factory.CephS3Factory;
import com.epam.digital.data.platform.integration.ceph.service.CephService;
import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.storage.form.config.CephStorageConfiguration;
import com.epam.digital.data.platform.storage.form.config.RedisStorageConfiguration;
import com.epam.digital.data.platform.storage.form.model.ApplicationContextType;
import com.epam.digital.data.platform.storage.form.model.CephKeysSearchParams;
import com.epam.digital.data.platform.storage.form.model.RedisKeysSearchParams;
import com.epam.digital.data.platform.storage.form.repository.ApplicationContextRepository;
import com.epam.digital.data.platform.storage.form.repository.CephFormDataRepository;
import com.epam.digital.data.platform.storage.form.repository.FormDataKeyValueRepository;
import com.epam.digital.data.platform.storage.form.repository.FormDataRepository;
import com.epam.digital.data.platform.storage.form.repository.RedisFormDataRepository;
import com.epam.digital.data.platform.storage.form.repository.impl.MongoStepsContextRepository;
import com.epam.digital.data.platform.storage.form.repository.impl.MongoVarsContextRepository;
import com.epam.digital.data.platform.storage.form.repository.impl.RedisApplicationContextRepository;
import com.epam.digital.data.platform.storage.form.service.ApplicationContextService;
import com.epam.digital.data.platform.storage.form.service.CephFormDataStorageService;
import com.epam.digital.data.platform.storage.form.service.ExternalContextService;
import com.epam.digital.data.platform.storage.form.service.FormDataKeyProviderImpl;
import com.epam.digital.data.platform.storage.form.service.FormDataStorageService;
import com.epam.digital.data.platform.storage.form.service.RedisFormDataStorageService;
import com.epam.digital.data.platform.storage.form.service.SerializationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Splitter;
import java.util.EnumMap;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisKeyValueAdapter;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.mapping.RedisMappingContext;
import org.springframework.data.redis.repository.support.RedisRepositoryFactory;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import io.lettuce.core.internal.HostAndPort;

/**
 * The class for creation storage services based on supported configuration
 */
public class StorageServiceFactory {

  private final ObjectMapper objectMapper;
  private CephS3Factory cephFactory;

  public StorageServiceFactory(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public StorageServiceFactory(ObjectMapper objectMapper,
                               CephS3Factory cephFactory) {
    this.objectMapper = objectMapper;
    this.cephFactory = cephFactory;
  }

  public FormDataStorageService<CephKeysSearchParams> formDataStorageService(CephStorageConfiguration config) {
    return CephFormDataStorageService.builder()
        .repository(newCephFormDataRepository(config))
        .keyProvider(newFormDataKeyProvider())
        .build();
  }

  public FormDataStorageService<RedisKeysSearchParams> formDataStorageService(
      RedisConnectionFactory connectionFactory) {
    return RedisFormDataStorageService.builder()
        .repository(newRedisFormDataRepository(connectionFactory))
        .keyProvider(newFormDataKeyProvider())
        .build();
  }

  public RedisConnectionFactory redisConnectionFactory(RedisStorageConfiguration configuration) {
    var redisSentinelConfig = new RedisSentinelConfiguration();
    redisSentinelConfig.setMaster(configuration.getSentinel().getMaster());
    setSentinelNodes(redisSentinelConfig, configuration);
    redisSentinelConfig.setUsername(configuration.getUsername());
    redisSentinelConfig.setPassword(configuration.getPassword());
    // Enable TLS connection options
    LettuceClientConfiguration.LettuceClientConfigurationBuilder lettuceConfigurationBuilder =
        LettuceClientConfiguration.builder();
    if (configuration.isSsl()) {
      lettuceConfigurationBuilder.useSsl();
    }
    var result =
        new LettuceConnectionFactory(redisSentinelConfig, lettuceConfigurationBuilder.build());
    result.afterPropertiesSet();
    return result;
  }

  public ApplicationContextService applicationContextService(
      String keyspace, RedisConnectionFactory connectionFactory) {
    var redisTemplate = newRedisTemplate(connectionFactory);

    var repositories = new EnumMap<ApplicationContextType, ApplicationContextRepository>
        (ApplicationContextType.class);
    for (ApplicationContextType value : ApplicationContextType.values()) {
      repositories.put(value, new RedisApplicationContextRepository(redisTemplate, keyspace,
          value.getName()));
    }

    return new ApplicationContextService(repositories);
  }

  public ApplicationContextService applicationExternalContextService(
      ExternalContextService service, IdmService idmService) {
    var repositories = new EnumMap<ApplicationContextType, ApplicationContextRepository>
        (ApplicationContextType.class);
    var serializationService = new SerializationService(objectMapper);
    repositories.put(ApplicationContextType.VARS, new MongoVarsContextRepository(service,
        serializationService, idmService));
    repositories.put(ApplicationContextType.STEPS, new MongoStepsContextRepository(service,
        serializationService, idmService));
    return new ApplicationContextService(repositories);
  }

  private FormDataKeyProviderImpl newFormDataKeyProvider() {
    return new FormDataKeyProviderImpl();
  }

  private FormDataRepository<CephKeysSearchParams> newCephFormDataRepository(CephStorageConfiguration config) {
    return CephFormDataRepository.builder()
        .cephBucketName(config.getBucket())
        .cephService(newCephServiceS3(config))
        .objectMapper(objectMapper)
        .build();
  }

  private CephService newCephServiceS3(CephStorageConfiguration config) {
    return cephFactory.createCephService(config.getHttpEndpoint(),
        config.getAccessKey(), config.getSecretKey());
  }

  private FormDataRepository<RedisKeysSearchParams> newRedisFormDataRepository(
      RedisConnectionFactory connectionFactory) {
    var template = newRedisTemplate(connectionFactory);

    return RedisFormDataRepository.builder()
        .repository(newFormDataKeyValueRepository(template))
        .template(template)
        .objectMapper(objectMapper)
        .build();
  }

  private FormDataKeyValueRepository newFormDataKeyValueRepository(
      RedisTemplate<String, Object> template) {
    RedisKeyValueAdapter keyValueAdapter = new RedisKeyValueAdapter(
        template.opsForHash().getOperations());
    RedisKeyValueTemplate keyValueTemplate = new RedisKeyValueTemplate(keyValueAdapter,
        new RedisMappingContext());

    RepositoryFactorySupport factory = new RedisRepositoryFactory(keyValueTemplate);
    return factory.getRepository(FormDataKeyValueRepository.class);
  }

  private RedisTemplate<String, Object> newRedisTemplate(RedisConnectionFactory connectionFactory) {
    RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(connectionFactory);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setValueSerializer(new StringRedisSerializer());
    redisTemplate.afterPropertiesSet();
    return redisTemplate;
  }

  private void setSentinelNodes(RedisSentinelConfiguration sentinelConfiguration,
                                RedisStorageConfiguration storageConfiguration) {
    var nodes = Splitter.on(',')
        .trimResults()
        .omitEmptyStrings()
        .splitToList(storageConfiguration.getSentinel().getNodes())
        .stream()
        .map(HostAndPort::parse)
        .toList();

    for (HostAndPort node : nodes) {
      sentinelConfiguration.sentinel(node.getHostText(), node.getPort());
    }
  }
}
