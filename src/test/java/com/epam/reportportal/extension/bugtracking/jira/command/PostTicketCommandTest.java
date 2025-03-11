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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.epam.reportportal.extension.bugtracking.jira.JiraStrategy;
import com.epam.reportportal.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.model.externalsystem.Ticket;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIf;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;


@Slf4j
class PostTicketCommandTest extends BaseCommandTest {

  @Autowired
  BasicTextEncryptor basicTextEncryptor;

/*  @Autowired
  @Qualifier("attachmentDataStoreService")
  DataStoreService dataStoreService;
  */
  @Mock
  TestItemRepository itemRepository;
  @Mock
  LogRepository logRepository;

  @Mock
  private BasicTextEncryptor simpleEncryptor;

  @InjectMocks
  JiraStrategy jiraStrategy;

  @Test
  @DisabledIf("disabled")
  void postTicketCommand() throws JsonProcessingException {
    TestItem testItem = new TestItem();
    when(itemRepository.findById(anyLong())).thenReturn(Optional.of(testItem));

    PostTicketRQ entity = objectMapper.readValue(DEFECT, PostTicketRQ.class);

/*    lenient().when(dataStoreService.load(anyString()))
        .thenReturn(Optional.of(getClass().getClassLoader().getResourceAsStream("attachment.txt")));*/

    Ticket ticket = jiraStrategy.submitTicket(entity, INTEGRATION);
    log.info(ticket.getTicketUrl());

    assertNotNull(ticket);
    verifyJiraTicket(ticket);

  }

  private void verifyJiraTicket(Ticket ticket) {
    String username = (String) INTEGRATION.getParams().getParams().get("email");
    String credentials = basicTextEncryptor.decrypt((String) INTEGRATION.getParams().getParams().get("password"));

    RestTemplate restTemplate = new RestTemplateBuilder()
        .basicAuthentication(username, credentials)
        .build();

    var jiraTicket = restTemplate.getForObject(ticket.getTicketUrl(), String.class);
    log.info(ticket.getTicketUrl());
    // TODO: make required checks with jira ticket
  }

}
