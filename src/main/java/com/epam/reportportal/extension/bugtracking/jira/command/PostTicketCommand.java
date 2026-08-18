package com.epam.reportportal.extension.bugtracking.jira.command;

import static com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.isNull;
import static com.epam.reportportal.base.infrastructure.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static com.epam.reportportal.extension.util.CommandParamUtils.ENTITY_PARAM;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.not;

import com.epam.reportportal.api.model.PluginCommandRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.PostFormField;
import com.epam.reportportal.base.infrastructure.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.base.infrastructure.model.externalsystem.Ticket;
import com.epam.reportportal.base.infrastructure.persistence.binary.DataStoreService;
import com.epam.reportportal.base.infrastructure.persistence.dao.LogRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.ProjectUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.TestItemRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationRepository;
import com.epam.reportportal.base.infrastructure.persistence.dao.organization.OrganizationUserRepository;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.Integration;
import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.persistence.entity.organization.OrganizationRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.project.ProjectRole;
import com.epam.reportportal.base.infrastructure.persistence.entity.user.UserRole;
import com.epam.reportportal.base.infrastructure.rules.commons.validation.Suppliers;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.bugtracking.jira.JIRATicketDescriptionService;
import com.epam.reportportal.extension.bugtracking.jira.JIRATicketUtils;
import com.epam.reportportal.extension.bugtracking.jira.JiraProps;
import com.epam.reportportal.extension.bugtracking.jira.api.model.CreatedIssue;
import com.epam.reportportal.extension.bugtracking.jira.api.model.IssueBean;
import com.epam.reportportal.extension.bugtracking.jira.api.model.IssueTypeDetails;
import com.epam.reportportal.extension.bugtracking.jira.api.model.IssueUpdateDetails;
import com.epam.reportportal.extension.bugtracking.jira.api.model.Project;
import com.epam.reportportal.extension.bugtracking.jira.api.model.SearchResults;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraClientProvider;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraRestClient;
import com.epam.reportportal.extension.bugtracking.jira.utils.IssueField;
import com.epam.reportportal.extension.command.AbstractExtensionCommand;
import com.epam.reportportal.extension.util.RequestEntityConverter;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ThreadUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.ByteArrayBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.jasypt.util.text.BasicTextEncryptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostTicketCommand extends AbstractExtensionCommand<Ticket> {

  private static final Logger LOGGER = LoggerFactory.getLogger(PostTicketCommand.class);

  private final JiraClientProvider clientProvider;
  private final DataStoreService dataStoreService;
  private final RequestEntityConverter requestEntityConverter;
  private final ObjectMapper objectMapper;
  private final BasicTextEncryptor basicTextEncryptor;
  private final Supplier<JIRATicketDescriptionService> descriptionService;

  public PostTicketCommand(JiraClientProvider clientProvider,
      DataStoreService dataStoreService,
      LogRepository logRepository,
      TestItemRepository testItemRepository,
      RequestEntityConverter requestEntityConverter,
      ObjectMapper objectMapper,
      BasicTextEncryptor basicTextEncryptor,
      ProjectRepository projectRepository,
      OrganizationUserRepository organizationUserRepository,
      OrganizationRepository organizationRepository,
      ProjectUserRepository projectUserRepository) {
    super(projectRepository, organizationUserRepository, organizationRepository, projectUserRepository);
    this.clientProvider = clientProvider;
    this.dataStoreService = dataStoreService;
    this.requestEntityConverter = requestEntityConverter;
    this.objectMapper = objectMapper;
    this.basicTextEncryptor = basicTextEncryptor;
    this.descriptionService = com.google.common.base.Suppliers.memoize(
        () -> new JIRATicketDescriptionService(logRepository, testItemRepository));
    this.minProjectRole = ProjectRole.EDITOR;
    this.minOrgRole = OrganizationRole.MANAGER;
    this.minUserRole = UserRole.ADMINISTRATOR;
  }

  @Override
  public String getName() {
    return "postTicket";
  }

  @Override
  protected Ticket invokeCommand(Integration integration, PluginCommandRQ pluginCommandRq) {
    PostTicketRQ ticketRQ = requestEntityConverter.getEntity(ENTITY_PARAM, pluginCommandRq.getArguments(),
        PostTicketRQ.class);
    expect(ticketRQ.getFields(), not(isNull()))
        .verify(UNABLE_INTERACT_WITH_INTEGRATION, "External System fields set is empty!");

    List<PostFormField> fields = ticketRQ.getFields();

    PostFormField issueType = new PostFormField();
    PostFormField components = new PostFormField();
    for (PostFormField field : fields) {
      if ("issuetype".equalsIgnoreCase(field.getId())) {
        issueType = field;
      }
      if ("components".equalsIgnoreCase(field.getId())) {
        components = field;
      }
    }

    expect(issueType.getValue().size(),
        com.epam.reportportal.base.infrastructure.persistence.commons.Predicates.equalTo(1))
        .verify(UNABLE_INTERACT_WITH_INTEGRATION,
            Suppliers.formattedSupplier("[IssueType] field has multiple values '{}' but should be only one",
                issueType.getValue()));

    final String issueTypeStr = issueType.getValue().get(0);

    IntegrationParams params = ofNullable(integration.getParams())
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
            "Integration params are not specified."));

    JiraRestClient client = clientProvider.provide(params);
    String projectKey = JiraProps.PROJECT.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Project is not specified."));
    Project jiraProject = client.projectsApi().getProject(projectKey, null, null);

    IssueTypeDetails projectIssueType = jiraProject.getIssueTypes().stream()
        .filter(input -> issueTypeStr.equalsIgnoreCase(input.getName()))
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
            Suppliers.formattedSupplier("Unable post issue with type '{}' for project '{}'.", issueTypeStr,
                projectKey)));

    IssueUpdateDetails issueRequest = JIRATicketUtils.toIssueInput(client, jiraProject, projectIssueType, ticketRQ,
        descriptionService.get(), objectMapper);

    Map<String, String> binaryData = findBinaryData(issueRequest);

    CreatedIssue createdIssue = client.issuesApi().createIssue(issueRequest, false);
    String issueKey = createdIssue.getKey();

    if (!binaryData.isEmpty()) {
      addAttachment(issueKey, params, binaryData);
    }

    SearchResults results = client.issueSearchApi()
        .searchForIssuesUsingJql("issue = " + issueKey, null, 50, "", null, null, null, false, false);
    if (results.getTotal() > 0) {
      IssueBean issue = client.issuesApi().getIssue(issueKey, null, null, null, null, null, null);
      String jiraUrl = JiraProps.URL.getParam(params)
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
      return JIRATicketUtils.toTicket(issue, jiraUrl, objectMapper);
    }
    return null;
  }

  private Map<String, String> findBinaryData(IssueUpdateDetails issueInput) {
    Map<String, String> binary = new HashMap<>();
    if (issueInput.getFields().get(IssueField.DESCRIPTION_FIELD.getValue()) != null) {
      String description = issueInput.getFields().get(IssueField.DESCRIPTION_FIELD.getValue()).toString();
      String regex = "(!|\\[\\^)\\w+\\.\\w{0,10}(\\||\\])";
      Matcher matcher = Pattern.compile(regex).matcher(description);
      while (matcher.find()) {
        String rawValue = description.subSequence(matcher.start(), matcher.end()).toString();
        String binaryDataName = rawValue.replace("!", "").replace("[", "").replace("]", "")
            .replace("^", "").replace("|", "");
        binary.put(binaryDataName.split("\\.")[0], binaryDataName);
      }
    }
    return binary;
  }

  @SneakyThrows
  private void addAttachment(String issueKey, IntegrationParams params, Map<String, String> binaryData) {
    String url = JiraProps.URL.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
    String username = JiraProps.USER_NAME.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Username is not specified."));
    String password = basicTextEncryptor.decrypt(JiraProps.PASSWORD.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Password is not specified.")));

    int count = 0;
    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
        .setLaxMode()
        .setCharset(StandardCharsets.UTF_8);

    for (Map.Entry<String, String> entry : binaryData.entrySet()) {
      ThreadUtils.sleep(Duration.of(1, ChronoUnit.SECONDS)); // 1 sec delay recommended by jira cloud support

      Optional<InputStream> data = dataStoreService.load(entry.getKey());
      if (data.isPresent()) {
        byte[] bytes = IOUtils.toByteArray(data.get());
        if (bytes.length == 0) {
          LOGGER.warn("Empty file {}", entry.getValue());
          continue;
        }
        entityBuilder.addPart("file", new ByteArrayBody(bytes, entry.getValue()));
        count++;
      }
      if (count > 0) {
        HttpPost request = new HttpPost(url + String.format("/rest/api/latest/issue/%s/attachments", issueKey));
        request.setEntity(entityBuilder.build());
        request.setHeader("X-Atlassian-Token", "no-check");
        String plainCreds = username + ":" + password;
        request.setHeader("Authorization",
            "Basic " + new String(Base64.encodeBase64(plainCreds.getBytes(StandardCharsets.UTF_8))));
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
          httpClient.execute(request, response -> {
            if (!(response.getCode() >= 200 && response.getCode() < 300)) {
              LOGGER.error("{} {}", response.getCode(), response.getReasonPhrase());
              throw new ReportPortalException("Failed to upload attachment for " + issueKey);
            }
            return response;
          });
        }
      }
    }
  }
}
