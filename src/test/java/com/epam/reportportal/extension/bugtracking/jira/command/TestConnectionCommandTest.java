/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.extension.bugtracking.jira.command;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraClientProvider;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

class TestConnectionCommandTest extends BaseCommandTest {

  @Autowired
  BasicTextEncryptor basicTextEncryptor;

  @Mock
  BasicTextEncryptor mockEncryptor;

  private TestConnectionCommand command;

  @BeforeEach
  void setUp() {
    lenient().when(mockEncryptor.decrypt(anyString()))
        .thenReturn((String) INTEGRATION.getParams().getParams().get("password"));
    command = new TestConnectionCommand(new JiraClientProvider(mockEncryptor),
        null, null, null, null);
  }

  @Test
  @DisabledIf("disabled")
  void testConnection() {
    Boolean result = command.invokeCommand(INTEGRATION, new PluginCommandRQ());
    assertTrue(result);
  }
}
