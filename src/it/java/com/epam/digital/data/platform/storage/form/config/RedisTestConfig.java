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

package com.epam.digital.data.platform.storage.form.config;

import com.epam.digital.data.platform.storage.form.factory.StorageServiceFactory;
import com.epam.digital.data.platform.storage.form.service.ApplicationContextService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.HostAndPort;
import redis.embedded.RedisCluster;

import java.io.IOException;
import java.util.List;

@SpringBootConfiguration
@EnableAutoConfiguration
@Profile("redis")
public class RedisTestConfig {

  private RedisCluster redisCluster;

  @Value("${redis.sentinel.master}")
  private String master;
  @Value("${redis.sentinel.nodes}")
  private String nodes;

  @PostConstruct
  public void postConstruct() throws IOException {
    var hostAndPort = HostAndPort.from(nodes);
    redisCluster =
        RedisCluster.newRedisCluster()
            .sentinelPorts(List.of(hostAndPort.getPort()))
            .sentinelCount(1)
            .quorumSize(1)
            .ephemeralServers()
            .replicationGroup(master, 1)
            .build();

    redisCluster.start();
  }

  @PreDestroy
  public void preDestroy() throws IOException {
    redisCluster.stop();
  }

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public StorageServiceFactory storageServiceFactory(ObjectMapper objectMapper) {
    return new StorageServiceFactory(objectMapper);
  }

  @Bean
  @ConfigurationProperties(prefix = "redis")
  public RedisStorageConfiguration redisStorageConfiguration() {
    return new RedisStorageConfiguration();
  }

  @Bean
  public RedisConnectionFactory redisConnectionFactory(StorageServiceFactory factory,
                                                       RedisStorageConfiguration config) {
    return factory.redisConnectionFactory(config);
  }

  @Bean
  public ApplicationContextService applicationContextService(
      StorageServiceFactory factory,
      RedisConnectionFactory redisConnectionFactory,
      @Value("${redis.keyspace}") String keyspace) {
    return factory.applicationContextService(keyspace, redisConnectionFactory);
  }

  @Bean
  public RedisTemplate<String, Object> redisTemplate(
      RedisConnectionFactory redisConnectionFactory) {
    RedisTemplate<String, Object> template = new RedisTemplate<>();
    template.setConnectionFactory(redisConnectionFactory);
    template.setKeySerializer(new StringRedisSerializer());
    template.setValueSerializer(new StringRedisSerializer());
    template.afterPropertiesSet();
    return template;
  }
}
