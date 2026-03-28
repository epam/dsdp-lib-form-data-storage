/*
 * Copyright 2026 EPAM Systems.
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

package com.epam.digital.data.platform.storage.form;

import com.epam.digital.data.platform.storage.form.config.MongoTestConfig;
import com.epam.digital.data.platform.storage.form.model.ApplicationStepInfo;
import com.epam.digital.data.platform.storage.form.service.ApplicationContextService;
import com.epam.digital.data.platform.storage.form.service.SerializationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.delete;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.ok;
import static com.github.tomakehurst.wiremock.client.WireMock.put;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {MongoTestConfig.class})
@ActiveProfiles("mongo")
public abstract class BaseMongoIT {

  @Autowired
  protected WireMockServer wireMockServer;

  @Autowired
  protected ApplicationContextService applicationContextService;

  protected SerializationService serializationService;

  @BeforeEach
  public void setUp() {
    serializationService = new SerializationService(new ObjectMapper());
    wireMockServer.resetAll();
  }

  protected void mockExternalContextGetVar(String key, Object value) {
    wireMockServer.stubFor(
        get(urlPathMatching(String.format("/v1alpha/applications/[^/]+/vars/%s", key)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(serializationService.serialize(value)))
    );
  }

  protected void mockExternalContextPutVar() {
    wireMockServer.stubFor(
        put(urlPathMatching("/v1alpha/applications/[^/]+/vars/[^/]+"))
            .willReturn(ok())
    );
  }

  protected void mockExternalContextPutStep() {
    wireMockServer.stubFor(
        put(urlPathMatching("/v1alpha/applications/[^/]+/steps/[^/]+"))
            .willReturn(ok())
    );
  }

  protected void mockExternalContextGetStep(String key, ApplicationStepInfo value) {
    wireMockServer.stubFor(
        get(urlPathMatching(String.format("/v1alpha/applications/[^/]+/steps/%s", key)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(serializationService.serialize(value)))
    );
  }

  protected void mockExternalContextDeleteStep(String key) {
    wireMockServer.stubFor(
        delete(urlPathMatching(String.format("/v1alpha/applications/[^/]+/steps/%s", key)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("true"))
    );
  }

  protected void mockExternalContextDeleteVar(String key) {
    wireMockServer.stubFor(
        delete(urlPathMatching(String.format("/v1alpha/applications/[^/]+/vars/%s", key)))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("true"))
    );
  }

  protected void mockExternalContextDeleteContext() {
    wireMockServer.stubFor(
        delete(urlPathMatching("/v1alpha/applications/[^/]+/vars"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody("true"))
    );
  }

  protected void mockExternalContextGetAllVars() {
    String serializedValue1 = serializationService.serialize("test-value");
    String serializedValue2 = serializationService.serialize("test-value");
    String jsonResponse = String.format("{\"test-var-key\":%s,\"varsKey-2\":%s}", serializedValue1, serializedValue2);
    wireMockServer.stubFor(
        get(urlPathMatching("/v1alpha/applications/[^/]+/vars"))
            .willReturn(aResponse()
                .withStatus(200)
                .withHeader("Content-Type", "application/json")
                .withBody(jsonResponse))
    );
  }

}
