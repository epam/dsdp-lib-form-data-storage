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

import com.epam.digital.data.platform.integration.idm.service.IdmService;
import com.epam.digital.data.platform.storage.form.factory.StorageServiceFactory;
import com.epam.digital.data.platform.storage.form.service.ApplicationContextService;
import com.epam.digital.data.platform.storage.form.service.ExternalContextService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import java.net.MalformedURLException;
import java.net.URL;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootConfiguration
@EnableAutoConfiguration
@EnableFeignClients(clients = ExternalContextService.class)
@Profile("mongo")
public class MongoTestConfig {

  @Bean
  public ObjectMapper objectMapper() {
    return new ObjectMapper();
  }

  @Bean
  public StorageServiceFactory storageServiceFactory(ObjectMapper objectMapper) {
    return new StorageServiceFactory(objectMapper);
  }

  @Bean
  public IdmService idmService() {
    IdmService idmService = mock(IdmService.class);
    when(idmService.getClientAccessToken()).thenReturn("test-token");
    return idmService;
  }

  @Bean
  public ApplicationContextService applicationContextService(
      StorageServiceFactory factory,
      ExternalContextService service,
      IdmService idmService) {
    return factory.applicationExternalContextService(service, idmService);
  }

  @Bean(destroyMethod = "stop")
  WireMockServer externalContextMockServer(@Value("${storage.externalContext.service.base-url}") String baseUrl) throws MalformedURLException {
    URL url = new URL(baseUrl);
    WireMockServer server = new WireMockServer(wireMockConfig().port(url.getPort()));
    server.start();
    WireMock.configureFor(url.getHost(), url.getPort());
    return server;
  }

}
