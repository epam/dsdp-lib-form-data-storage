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


import java.util.Map;
import java.util.Optional;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import feign.Response;

@FeignClient(
    name = "externalContextService",
    url = "${storage.externalContext.service.base-url}"
)
public interface ExternalContextService {

  @PutMapping(value = "/{id}/steps/{stepId}", consumes = "application/json", produces =
      "application/json")
  void putStep(@PathVariable("id") String id,
               @PathVariable("stepId") String stepId,
               @RequestBody String stepInfo,
               @RequestHeader("X-Access-Token") String accessToken);


  @GetMapping(value = "/{id}/steps/{stepId}", produces = "application/json")
  Optional<String> getStep(@PathVariable("id") String id,
                           @PathVariable("stepId") String stepId,
                           @RequestHeader("X-Access-Token") String accessToken);

  @GetMapping(value = "/{id}/steps", produces = "application/json")
  Map<String, Object> getAllSteps(@PathVariable("id") String id,
                                  @RequestHeader("X-Access-Token") String accessToken);

  @DeleteMapping(value = "/{id}/steps/{stepId}", produces = "application/json")
  Boolean deleteStep(@PathVariable("id") String id,
                     @PathVariable("stepId") String stepId,
                     @RequestHeader("X-Access-Token") String accessToken);

  @DeleteMapping(value = "/{id}/steps", produces = "application/json")
  Boolean deleteStepsContext(@PathVariable("id") String id,
                             @RequestHeader("X-Access-Token") String accessToken);

  @PutMapping(value = "/{id}/vars/{key}", consumes = "application/json")
  void putVar(@PathVariable("id") String id,
              @PathVariable("key") String key,
              @RequestBody Object value,
              @RequestHeader("X-Access-Token") String accessToken);

  @GetMapping(value = "/{id}/vars/{key}", produces = "application/json")
  Optional<String> getVar(@PathVariable("id") String id,
                          @PathVariable("key") String key,
                          @RequestHeader("X-Access-Token") String accessToken);

  @GetMapping(value = "/{id}/vars", produces = "application/json")
  Map<String, Object> getAllVars(@PathVariable("id") String id,
                                 @RequestHeader("X-Access-Token") String accessToken);

  @DeleteMapping(value = "/{id}/vars/{stepId}", produces = "application/json")
  Boolean deleteVar(@PathVariable("id") String id,
                    @PathVariable("stepId") String stepId,
                    @RequestHeader("X-Access-Token") String accessToken);

  @DeleteMapping(value = "/{id}/vars", produces = "application/json")
  Boolean deleteVarsContext(@PathVariable("id") String id,
                            @RequestHeader("X-Access-Token") String accessToken);


}
