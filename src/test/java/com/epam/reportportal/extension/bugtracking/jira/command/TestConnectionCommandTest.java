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
import static org.mockito.Mockito.when;

import com.epam.reportportal.extension.bugtracking.jira.JiraProps;
import com.epam.reportportal.extension.bugtracking.jira.JiraStrategy;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.mockito.InjectMocks;
import org.mockito.Mock;

class TestConnectionCommandTest extends BaseCommandTest {

  @Mock
  private BasicTextEncryptor simpleEncryptor;
  @InjectMocks
  JiraStrategy jiraStrategy;


  @Test
  @Disabled
  void testConnection() {
    when(simpleEncryptor.decrypt(anyString())).thenReturn(JiraProps.PASSWORD.getParam(INTEGRATION.getParams()).get());

    boolean response = jiraStrategy.testConnection(INTEGRATION);
    assertTrue(response);
  }
}
