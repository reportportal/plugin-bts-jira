package com.epam.reportportal.extension.bugtracking.jira.command;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.dao.IntegrationRepository;
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
import com.epam.reportportal.extension.bugtracking.jira.client.JiraClientProvider;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraRestClient;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GetIssueCommand extends AbstractExtensionCommand<Ticket> {

  private static final String TICKET_ID = "ticketId";
  private static final String PROJECT_ID = "projectId";

  private final JiraClientProvider clientProvider;
  private final ObjectMapper objectMapper;
  private final IntegrationRepository integrationRepository;


  public GetIssueCommand(JiraClientProvider clientProvider,
      ObjectMapper objectMapper,
      ProjectRepository projectRepository,
      OrganizationUserRepository organizationUserRepository,
      OrganizationRepository organizationRepository,
      ProjectUserRepository projectUserRepository,
      IntegrationRepository integrationRepository) {
    super(projectRepository, organizationUserRepository, organizationRepository, projectUserRepository);
    this.clientProvider = clientProvider;
    this.objectMapper = objectMapper;
    this.integrationRepository = integrationRepository;
    this.minProjectRole = ProjectRole.EDITOR;
    this.minOrgRole = OrganizationRole.MANAGER;
    this.minUserRole = UserRole.ADMINISTRATOR;
  }

  @Override
  public String getName() {
    return "getIssue";
  }

  @Override
  protected Ticket invokeCommand(PluginCommandRQ pluginCommandRq) {
    Map<String, Object> params = pluginCommandRq.getArguments();
    var ticketId = (String) ofNullable(params.get(TICKET_ID))
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, TICKET_ID + " must be provided"));

    final Long projectId = (Long) ofNullable(params.get(PROJECT_ID))
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
            PROJECT_ID + " must be provided"));

    String btsUrl = (String) params.get("url");
    String btsProject = (String) params.get("project");

    Integration integration =
        integrationRepository.findProjectBtsByUrlAndLinkedProject(btsUrl, btsProject, projectId)
            .orElseGet(() -> integrationRepository.findGlobalBtsByUrlAndLinkedProject(btsUrl, btsProject)
                .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR,
                    "Integration with provided url and project isn't found")));
    IntegrationParams integrationParams = ofNullable(integration.getParams())
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
            "Integration params are not specified."));
    String jiraUrl = JiraProps.URL.getParam(integrationParams)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));

    JiraRestClient client = clientProvider.provide(integrationParams);
    IssueBean issueBean;
    try {
      issueBean = client.issuesApi().getIssue(ticketId, List.of("summary", "status"), false, null, null, false, false);
    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR);
    }
    if (Objects.nonNull(issueBean)) {
      return JIRATicketUtils.toTicket(issueBean, jiraUrl, objectMapper);
    } else {
      throw new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, "Ticket with id {} is not found", ticketId);
    }
  }
}
