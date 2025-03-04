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

import static com.epam.reportportal.extension.bugtracking.jira.utils.TestProperties.getTestProperties;

import com.epam.ta.reportportal.dao.ProjectRepository;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.util.text.BasicTextEncryptor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// set values in 'integration.properties' to enable the tests
@Slf4j
@ExtendWith(MockitoExtension.class)
@Disabled
public abstract class BaseCommandTest {

  @Mock
  ProjectRepository projectRepository;


  public static final Map<String, Object> JIRA_COMMAND_PARAMS = new HashMap<>();
  public static final String TICKET_ID_FIELD = "ticketId";
  public static final String PROJECT_ID_FIELD = "projectId";
  public static final Integration INTEGRATION = new Integration();

  public static BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
  public static ObjectMapper objectMapper = new ObjectMapper();

  static {
    basicTextEncryptor.setPassword("123");

    objectMapper.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
    objectMapper.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    objectMapper.registerModule(new JavaTimeModule());
  }

  @BeforeAll
  protected static void before() {

    Properties integrationProps = getTestProperties("integration.properties");
    Map<String, Object> params = new HashMap<>();
    integrationProps.keySet()
        .stream()
        .map(String::valueOf)
        .forEach(key -> {
          if (key.equals("apiToken")) {
            params.put(key, basicTextEncryptor.encrypt(integrationProps.getProperty(key)));
          } else {
            params.put(key, integrationProps.getProperty(key));
          }
        });
    INTEGRATION.setParams(new IntegrationParams(params));

    Properties jobProps = getTestProperties("jira-project.properties");
    if (StringUtils.isNoneBlank((String) jobProps.get(TICKET_ID_FIELD))) {
      JIRA_COMMAND_PARAMS.put(TICKET_ID_FIELD, jobProps.get(TICKET_ID_FIELD)); // EPMRPP-000000000
    }
    if (StringUtils.isNoneBlank((String) jobProps.get(PROJECT_ID_FIELD))) {
      JIRA_COMMAND_PARAMS.put(PROJECT_ID_FIELD, Long.valueOf(String.valueOf(jobProps.get(PROJECT_ID_FIELD)))); // Long
    }
  }

  protected boolean disabled() {
    return INTEGRATION.getParams().getParams().values()
        .stream()
        .map(String::valueOf)
        .anyMatch(StringUtils::isEmpty);
  }

}
