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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraClientProvider;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.mockito.Mock;

class GetIssueCommandTest extends BaseCommandTest {

  @Mock
  BasicTextEncryptor mockEncryptor;

  @Mock
  IntegrationRepository integrationRepository;

  private GetIssueCommand command;

  @BeforeEach
  void setUp() {
    lenient().when(mockEncryptor.decrypt(anyString()))
        .thenReturn((String) INTEGRATION.getParams().getParams().get("password"));

    when(integrationRepository.findProjectBtsByUrlAndLinkedProject(anyString(), anyString(), anyLong()))
        .thenReturn(Optional.of(INTEGRATION));

    command = new GetIssueCommand(new JiraClientProvider(mockEncryptor), objectMapper,
        null, null, null, null, integrationRepository);
  }

  @Test
  @DisabledIf("disabled")
  void getIssueCommand() {
    Map<String, Object> args = new HashMap<>();
    args.put("ticketId", JIRA_COMMAND_PARAMS.get(TICKET_ID_FIELD));

    PluginCommandRQ rq = new PluginCommandRQ();
    rq.setArguments(args);

    Ticket result = command.invokeCommand(rq);
    assertNotNull(result);
  }
}
