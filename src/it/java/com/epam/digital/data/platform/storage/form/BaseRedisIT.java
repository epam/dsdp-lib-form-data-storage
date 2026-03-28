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

import com.epam.digital.data.platform.storage.form.config.RedisTestConfig;
import com.epam.digital.data.platform.storage.form.service.ApplicationContextService;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {RedisTestConfig.class})
@ActiveProfiles("redis")
public abstract class BaseRedisIT {

  @Autowired
  protected ApplicationContextService applicationContextService;

  @Autowired
  protected RedisTemplate<String, Object> redisTemplate;

  @BeforeEach
  public void setUp() {
  }

}
