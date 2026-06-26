package com.epam.reportportal.extension.bugtracking.jira.command;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static com.epam.reportportal.extension.bugtracking.jira.utils.IssueField.AFFECTS_VERSIONS_FIELD;
import static com.epam.reportportal.extension.bugtracking.jira.utils.IssueField.COMPONENTS_FIELD;
import static com.epam.reportportal.extension.bugtracking.jira.utils.IssueField.FIX_VERSIONS_FIELD;
import static com.epam.reportportal.extension.bugtracking.jira.utils.IssueField.PRIORITY_FIELD;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.AllowedValue;
import com.epam.reportportal.base.infrastructure.model.externalsystem.PostFormField;
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
import com.epam.reportportal.extension.bugtracking.jira.api.model.IssueTypeDetails;
import com.epam.reportportal.extension.bugtracking.jira.api.model.PageOfCreateMetaIssueTypeWithField;
import com.epam.reportportal.extension.bugtracking.jira.api.model.Project;
import com.epam.reportportal.extension.bugtracking.jira.api.model.ProjectComponent;
import com.epam.reportportal.extension.bugtracking.jira.api.model.Version;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraClientProvider;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraRestClient;
import com.epam.reportportal.extension.bugtracking.jira.utils.IssueField;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.StreamSupport;

public class GetIssueFieldsCommand extends AbstractExtensionCommand<List<PostFormField>> {

  private static final String ISSUE_TYPE_PARAM = "issuetype";

  private final JiraClientProvider clientProvider;
  private final ObjectMapper objectMapper;

  public GetIssueFieldsCommand(JiraClientProvider clientProvider,
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
    return "getIssueFields";
  }

  @Override
  protected List<PostFormField> invokeCommand(Integration integration, PluginCommandRQ pluginCommandRq) {
    String ticketType = (String) ofNullable(pluginCommandRq.getArguments().get(ISSUE_TYPE_PARAM))
        .orElseThrow(() -> new ReportPortalException(ErrorType.BAD_REQUEST_ERROR, ISSUE_TYPE_PARAM + " must be provided"));

    IntegrationParams params = ofNullable(integration.getParams())
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
            "Integration params are not specified."));

    JiraRestClient client = clientProvider.provide(params);
    String projectKey = JiraProps.PROJECT.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Project is not specified."));
    Project jiraProject = client.projectsApi().getProject(projectKey, null, null);

    IssueTypeDetails issueType = jiraProject.getIssueTypes().stream()
        .filter(input -> ticketType.equalsIgnoreCase(input.getName()))
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
            "Issue type '" + ticketType + "' not found"));

    PageOfCreateMetaIssueTypeWithField issueCreateMetadata = client.issuesApi()
        .getCreateIssueMetaIssueTypeId(jiraProject.getId(), issueType.getId(), 0, 1000);

    List<PostFormField> result = new ArrayList<>();
    ((List<Map<String, Object>>) issueCreateMetadata.getAdditionalProperties().get("values")).stream()
        .map(a -> objectMapper.convertValue(a, new TypeReference<Map<String, Object>>() {}))
        .map(field -> (JsonNode) objectMapper.valueToTree(field))
        .forEach(jsonField -> {
          String fieldID = jsonField.get("fieldId").asText();
          String fieldName = jsonField.get("name").asText();
          String fieldType = jsonField.get("schema").get("type").asText();
          boolean required = jsonField.get("required").asBoolean();

          if ("project".equalsIgnoreCase(fieldID)
              || "attachment".equalsIgnoreCase(fieldID)
              || "timetracking".equalsIgnoreCase(fieldID)
              || "Epic Link".equalsIgnoreCase(fieldName)
              || "Sprint".equalsIgnoreCase(fieldName)) {
            return;
          }

          List<AllowedValue> allowedList = new ArrayList<>();
          if (jsonField.get("allowedValues") != null) {
            allowedList.addAll(StreamSupport.stream(jsonField.get("allowedValues").spliterator(), false)
                .filter(JIRATicketUtils::isCustomField)
                .map(allowedType -> new AllowedValue(allowedType.get("id").asText(),
                    allowedType.get("value").asText()))
                .toList());
          }

          if (fieldID.equalsIgnoreCase(COMPONENTS_FIELD.getValue())) {
            for (ProjectComponent component : jiraProject.getComponents()) {
              allowedList.add(new AllowedValue(component.getId(), component.getName()));
            }
          }
          if (fieldID.equalsIgnoreCase(FIX_VERSIONS_FIELD.getValue()) || fieldID.equalsIgnoreCase(AFFECTS_VERSIONS_FIELD.getValue())) {
            for (Version version : jiraProject.getVersions()) {
              allowedList.add(new AllowedValue(version.getId(), version.getName()));
            }
          }
          if (fieldID.equalsIgnoreCase(PRIORITY_FIELD.getValue()) && jsonField.get("allowedValues") != null) {
            allowedList.addAll(StreamSupport.stream(jsonField.get("allowedValues").spliterator(), false)
                .map(allowedType -> new AllowedValue(allowedType.get("id").asText(),
                    allowedType.get("name").asText()))
                .toList());
          }

          List<String> defValue = fieldID.equalsIgnoreCase(IssueField.ISSUE_TYPE_FIELD.getValue())
              ? Collections.singletonList(ticketType) : null;

          result.add(new PostFormField(fieldID, fieldName, fieldType, required, defValue, allowedList));
        });
    return result;
  }
}
