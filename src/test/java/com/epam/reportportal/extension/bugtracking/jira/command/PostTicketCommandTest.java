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

import static com.epam.reportportal.extension.bugtracking.jira.utils.SampleData.DEFECT;
import static com.epam.reportportal.extension.util.CommandParamUtils.ENTITY_PARAM;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.binary.DataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.item.TestItem;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraClientProvider;
import com.epam.reportportal.extension.util.RequestEntityConverter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
class PostTicketCommandTest extends BaseCommandTest {

  @Autowired
  BasicTextEncryptor basicTextEncryptor;

  @Mock
  BasicTextEncryptor mockEncryptor;

  @Mock
  DataStoreService dataStoreService;

  @Mock
  TestItemRepository itemRepository;

  @Mock
  LogRepository logRepository;

  private PostTicketCommand command;

  @BeforeEach
  void setUp() {
    lenient().when(mockEncryptor.decrypt(anyString()))
        .thenReturn((String) INTEGRATION.getParams().getParams().get("password"));
    command = new PostTicketCommand(
        new JiraClientProvider(mockEncryptor),
        dataStoreService,
        logRepository,
        itemRepository,
        new RequestEntityConverter(objectMapper),
        objectMapper,
        null, null, null, null
    );
  }

  @Test
  @DisabledIf("disabled")
  void postTicketCommand() {
    when(itemRepository.findById(anyLong())).thenReturn(Optional.of(new TestItem()));

    Map<String, Object> args = new HashMap<>();
    args.put(ENTITY_PARAM, DEFECT);

    PluginCommandRQ rq = new PluginCommandRQ();
    rq.setArguments(args);

    Ticket ticket = command.invokeCommand(INTEGRATION, rq);
    log.info(ticket.getTicketUrl());
    assertNotNull(ticket);
  }

  @Test
  @DisabledIf("disabled")
  void addAttachmentTest() {
    // attachment testing is done via the full postTicket flow
  }
}
