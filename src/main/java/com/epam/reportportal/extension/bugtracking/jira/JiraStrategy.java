/*
 * Copyright 2016 EPAM Systems
 *
 *
 * This file is part of EPAM Report Portal.
 * https://github.com/reportportal/service-jira
 *
 * Report Portal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Report Portal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Report Portal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.reportportal.extension.bugtracking.jira;

import com.epam.reportportal.base.infrastructure.persistence.binary.DataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.IntegrationGroupEnum;
import com.epam.reportportal.extension.NamedPluginCommand;
import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraClientProvider;
import com.epam.reportportal.extension.bugtracking.jira.command.GetIssueCommand;
import com.epam.reportportal.extension.bugtracking.jira.command.GetIssueFieldsCommand;
import com.epam.reportportal.extension.bugtracking.jira.command.GetIssueTypesCommand;
import com.epam.reportportal.extension.bugtracking.jira.command.PostTicketCommand;
import com.epam.reportportal.extension.bugtracking.jira.command.TestConnectionCommand;
import com.epam.reportportal.extension.command.ExtensionCommand;
import com.epam.reportportal.extension.util.RequestEntityConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jasypt.util.text.BasicTextEncryptor;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Extension
@Component
public class JiraStrategy implements ReportPortalExtensionPoint {

  private static final String DOCUMENTATION_LINK_FIELD = "documentationLink";
  private static final String DOCUMENTATION_LINK =
      "https://reportportal.io/docs/plugins/AtlassianJiraServer";
  private static final String NAME_FIELD = "name";
  private static final String PLUGIN_NAME = "Jira Server";

  @Autowired
  @Qualifier("attachmentDataStoreService")
  private DataStoreService dataStoreService;

  @Autowired
  private BasicTextEncryptor basicTextEncryptor;

  @Autowired
  private LogRepository logRepository;

  @Autowired
  private TestItemRepository itemRepository;

  @Autowired
  private ProjectRepository projectRepository;

  @Autowired
  private OrganizationUserRepository organizationUserRepository;

  @Autowired
  private OrganizationRepository organizationRepository;

  @Autowired
  private ProjectUserRepository projectUserRepository;

  @Autowired
  private ObjectMapper objectMapper;

  private final Supplier<Map<String, ExtensionCommand<?>>> commandMapping =
      com.google.common.base.Suppliers.memoize(this::buildCommandMapping);

  @Override
  public Map<String, ?> getPluginParams() {
    Map<String, Object> params = new HashMap<>();
    params.put(DOCUMENTATION_LINK_FIELD, DOCUMENTATION_LINK);
    params.put(NAME_FIELD, PLUGIN_NAME);
    params.put(ALLOWED_COMMANDS, new ArrayList<>(getIntegrationExtensionCommands().keySet()));
    return params;
  }

  @Override
  public CommonPluginCommand<?> getCommonCommand(String commandName) {
    return null;
  }

  @Override
  public PluginCommand<?> getIntegrationCommand(String commandName) {
    return null;
  }

  @Override
  public IntegrationGroupEnum getIntegrationGroup() {
    return IntegrationGroupEnum.BTS;
  }

  @Override
  public Map<String, ExtensionCommand<?>> getIntegrationExtensionCommands() {
    return commandMapping.get();
  }

  private Map<String, ExtensionCommand<?>> buildCommandMapping() {
    JiraClientProvider clientProvider = new JiraClientProvider(basicTextEncryptor);
    RequestEntityConverter requestEntityConverter = new RequestEntityConverter(objectMapper);

    List<ExtensionCommand<?>> commands = List.of(
        new TestConnectionCommand(clientProvider, projectRepository, organizationUserRepository,
            organizationRepository, projectUserRepository),
        new GetIssueCommand(clientProvider, objectMapper, projectRepository, organizationUserRepository,
            organizationRepository, projectUserRepository),
        new GetIssueTypesCommand(clientProvider, projectRepository, organizationUserRepository,
            organizationRepository, projectUserRepository),
        new GetIssueFieldsCommand(clientProvider, objectMapper, projectRepository, organizationUserRepository,
            organizationRepository, projectUserRepository),
        new PostTicketCommand(clientProvider, dataStoreService, logRepository, itemRepository,
            requestEntityConverter, objectMapper, projectRepository, organizationUserRepository,
            organizationRepository, projectUserRepository)
    );

    return commands.stream().collect(Collectors.toMap(NamedPluginCommand::getName, it -> it));
  }
}
