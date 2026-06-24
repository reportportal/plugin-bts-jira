package com.epam.reportportal.extension.bugtracking.jira.command;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.exception.ErrorType;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.bugtracking.jira.JIRATicketUtils;
import com.epam.reportportal.extension.bugtracking.jira.JiraProps;
import com.epam.reportportal.extension.bugtracking.jira.api.model.IssueBean;
import com.epam.reportportal.extension.bugtracking.jira.api.model.SearchResults;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraClientProvider;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraRestClient;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GetIssueCommand extends AbstractExtensionCommand<Ticket> {

  private static final String TICKET_ID = "ticketId";

  private final JiraClientProvider clientProvider;
  private final ObjectMapper objectMapper;

  public GetIssueCommand(JiraClientProvider clientProvider,
      ObjectMapper objectMapper,
      ProjectRepository projectRepository,
      OrganizationUserRepository organizationUserRepository,
      OrganizationRepository organizationRepository,
      ProjectUserRepository projectUserRepository) {
    super(projectRepository, organizationUserRepository, organizationRepository, projectUserRepository);
    this.clientProvider = clientProvider;
    this.objectMapper = objectMapper;
    this.minProjectRole = ProjectRole.EDITOR;
    this.minOrgRole = OrganizationRole.MANAGER;
    this.minUserRole = UserRole.ADMINISTRATOR;
  }

  @Override
  public String getName() {
    return "getIssue";
  }

  @Override
  protected Ticket invokeCommand(Integration integration, PluginCommandRQ pluginCommandRq) {
    String ticketId = (String) ofNullable(pluginCommandRq.getArguments().get(TICKET_ID))
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, TICKET_ID + " must be provided"));

    IntegrationParams params = ofNullable(integration.getParams())
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
            "Integration params are not specified."));

    JiraRestClient client = clientProvider.provide(params);
    SearchResults results = client.issueSearchApi()
        .searchForIssuesUsingJql("issue = " + ticketId, null, 50, "", null, null, null, false, false);

    if (results.getTotal() == 0) {
      throw new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Ticket not found: " + ticketId);
    }

    IssueBean issue = client.issuesApi().getIssue(ticketId, null, null, null, null, null, null);
    String jiraUrl = JiraProps.URL.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
    return JIRATicketUtils.toTicket(issue, jiraUrl, objectMapper);
  }
}
