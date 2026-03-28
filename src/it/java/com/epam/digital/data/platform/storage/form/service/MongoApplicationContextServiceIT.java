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

package com.epam.digital.data.platform.storage.form.service;

import com.epam.digital.data.platform.storage.form.BaseMongoIT;
import com.epam.digital.data.platform.storage.form.model.ApplicationStepInfo;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.putRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.deleteRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MongoApplicationContextServiceIT extends BaseMongoIT {

  private static final String CONTEXT_ID = "test-context-id";
  private static final String ENTRY_VALUE = "test-value";
  private static final String VAR_KEY = "test-var-key";
  private static final String STEP_KEY = "test-step-key";

  @Override
  @BeforeEach
  public void setUp() {
    super.setUp();
  }

  @Test
  @DisplayName("Should put and retrieve var entry")
  void shouldPutAndGetVar() {
    mockExternalContextPutVar();
    mockExternalContextGetVar(VAR_KEY, ENTRY_VALUE);

    applicationContextService.vars().put(CONTEXT_ID, VAR_KEY, ENTRY_VALUE);
    Optional<String> result = applicationContextService.vars().get(CONTEXT_ID, VAR_KEY);

    assertTrue(result.isPresent());
    assertEquals(ENTRY_VALUE, result.get());
    verify(putRequestedFor(urlPathMatching("/v1alpha/applications/.*/vars/.*")));
    verify(getRequestedFor(urlPathMatching("/v1alpha/applications/.*/vars/.*")));
  }

  @Test
  @DisplayName("Should put and retrieve step entry")
  void shouldPutAndGetStep() {
    ApplicationStepInfo stepInfo = new ApplicationStepInfo(
        "test",
        ApplicationStepInfo.StepType.FORM_SUBMISSION,
        ApplicationStepInfo.Status.PENDING,
        null,
        null
    );

    mockExternalContextPutStep();
    mockExternalContextGetStep(STEP_KEY, stepInfo);

    applicationContextService.steps().put(CONTEXT_ID, STEP_KEY, stepInfo);
    Optional<ApplicationStepInfo> result = applicationContextService.steps().get(CONTEXT_ID, STEP_KEY);

    assertTrue(result.isPresent());
    assertEquals(stepInfo, result.get());
    verify(putRequestedFor(urlPathMatching("/v1alpha/applications/.*/steps/.*")));
    verify(getRequestedFor(urlPathMatching("/v1alpha/applications/.*/steps/.*")));
  }

  @Test
  @DisplayName("Should delete var entry")
  void shouldDeleteVar() {
    mockExternalContextPutVar();
    mockExternalContextDeleteVar(VAR_KEY);

    applicationContextService.vars().put(CONTEXT_ID, VAR_KEY, ENTRY_VALUE);
    boolean deleted = applicationContextService.vars().delete(CONTEXT_ID, VAR_KEY);

    assertTrue(deleted);
    verify(putRequestedFor(urlPathMatching("/v1alpha/applications/.*/vars/.*")));
    verify(deleteRequestedFor(urlPathMatching("/v1alpha/applications/.*/vars/.*")));
  }

  @Test
  @DisplayName("Should check if var entry exists")
  void shouldCheckVarEntryExists() {
    mockExternalContextGetVar(VAR_KEY, ENTRY_VALUE);

    boolean exists = applicationContextService.vars().exists(CONTEXT_ID, VAR_KEY);

    assertTrue(exists);
    verify(getRequestedFor(urlPathMatching("/v1alpha/applications/.*/vars/.*")));
  }

  @Test
  @DisplayName("Should get all var entries")
  void shouldGetAllVars() {
    mockExternalContextGetAllVars();

    var allEntries = applicationContextService.vars().getAll(CONTEXT_ID);

    assertFalse(allEntries.isEmpty());
    assertEquals(2, allEntries.size());
    verify(getRequestedFor(urlPathMatching("/v1alpha/applications/.*/vars")));
  }

  @Test
  @DisplayName("Should delete entire vars context")
  void shouldDeleteVarsContext() {
    mockExternalContextDeleteContext();

    boolean deleted = applicationContextService.vars().deleteContext(CONTEXT_ID);

    assertTrue(deleted);
    verify(deleteRequestedFor(urlPathMatching("/v1alpha/applications/.*/vars")));
  }

  @Test
  @DisplayName("Should support steps exists and delete")
  void shouldStepsExistsAndDelete() {
    ApplicationStepInfo stepInfo = new ApplicationStepInfo(
        "test",
        ApplicationStepInfo.StepType.FORM_SUBMISSION,
        ApplicationStepInfo.Status.PENDING,
        null,
        null
    );

    mockExternalContextPutStep();
    mockExternalContextGetStep(STEP_KEY, stepInfo);
    mockExternalContextDeleteStep(STEP_KEY);

    applicationContextService.steps().put(CONTEXT_ID, STEP_KEY, stepInfo);
    assertTrue(applicationContextService.steps().exists(CONTEXT_ID, STEP_KEY));
    assertTrue(applicationContextService.steps().delete(CONTEXT_ID, STEP_KEY));

    verify(putRequestedFor(urlPathMatching("/v1alpha/applications/.*/steps/.*")));
    verify(getRequestedFor(urlPathMatching("/v1alpha/applications/.*/steps/.*")));
    verify(deleteRequestedFor(urlPathMatching("/v1alpha/applications/.*/steps/.*")));
  }
}