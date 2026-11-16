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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.PostFormField;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraClientProvider;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;

class GetIssueFieldsCommandTest extends BaseCommandTest {

  @Mock
  BasicTextEncryptor mockEncryptor;

  private GetIssueFieldsCommand command;

  @BeforeEach
  void setUp() {
    lenient().when(mockEncryptor.decrypt(anyString()))
        .thenReturn((String) INTEGRATION.getParams().getParams().get("password"));
    command = new GetIssueFieldsCommand(new JiraClientProvider(mockEncryptor), objectMapper,
        null, null, null, null);
  }

  @ParameterizedTest
  @CsvSource(value = {
      "Defect",
      "Epic",
      "Test",
      "User Story",
      "Technical Story",
      "Spike",
      "Defect",
      "Sub-task",
      "Task",
      "Security issue",
      "Cloud issue"
  })
  void getIssueFields(String issueType) {
    if (disabled()) {
      return;
    }

    Map<String, Object> args = new HashMap<>();
    args.put("issuetype", issueType);

    PluginCommandRQ rq = new PluginCommandRQ();
    rq.setArguments(args);

    List<PostFormField> result = command.invokeCommand(INTEGRATION, rq);
    assertFalse(result.isEmpty());
  }
}
