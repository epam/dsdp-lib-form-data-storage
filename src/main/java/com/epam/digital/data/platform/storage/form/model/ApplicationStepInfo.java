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

package com.epam.digital.data.platform.storage.form.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * Represents information about a specific step within an application workflow process.
 * <p>
 * This immutable record encapsulates the attributes of an application step,
 * including its unique identifier, type classification, current processing status,
 * and additional contextual details that may be required for step execution.
 * </p>
 *
 * @param id      the unique identifier of the application step, must not be null or empty.
 *                This identifier should be unique within the context of a single application
 *                workflow and is used for step tracking and reference purposes.
 * @param type    the classification or category of the application step, indicating the
 *                nature of processing or operation being performed. Must not be null or empty.
 * @param status  the current processing status of the application step, represented by
 *                the {@link Status} enum. This provides type-safe status representation
 *                and ensures only valid status values can be assigned to a step.
 *                Must not be null.
 * @param details a flexible map containing additional contextual information, metadata,
 *                or configuration parameters specific to this application step.
 *                The content and structure of this map depend on the step type and
 *                processing requirements. May contain execution results, error messages,
 *                timestamps, configuration values, or any other relevant data.
 *                Can be null or empty if no additional details are required.
 * @see Status
 */
public record ApplicationStepInfo(
    String id,
    StepType type,
    Status status,
    Form form,
    Map<String, Object> details
) implements Serializable {

  /**
   * Enumeration representing the possible statuses of an application step within
   * a workflow process.
   * <p>
   * This enum provides type-safe status representation and ensures consistent
   * status management across the application workflow system. Each status represents
   * a distinct state in the step lifecycle, from initial creation through final
   * completion or termination.
   * </p>
   */
  public enum Status {
    PENDING,
    SUCCESS,
    SUSPENDED,
    REJECTED
  }

  public enum StepType {
    FORM_SUBMISSION,
    SIGNATURE_UPLOAD,
    SIMA,
    ASAN_IMZA
  }

  @Builder
  @Getter
  public static class Form implements Serializable {
    private final String key;
    @Setter
    private String token;
    private String language;
    private final Map<String, Object> evalContext;
    private final Map<String, Object> data;
  }
}
