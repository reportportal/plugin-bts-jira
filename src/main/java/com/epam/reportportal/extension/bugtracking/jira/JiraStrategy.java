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

import static com.epam.reportportal.extension.bugtracking.jira.utils.IssueField.AFFECTS_VERSIONS_FIELD;
import static com.epam.reportportal.extension.bugtracking.jira.utils.IssueField.COMPONENTS_FIELD;
import static com.epam.reportportal.extension.bugtracking.jira.utils.IssueField.FIX_VERSIONS_FIELD;
import static com.epam.reportportal.extension.bugtracking.jira.utils.IssueField.PRIORITY_FIELD;
import static com.epam.reportportal.rules.commons.validation.BusinessRule.expect;
import static com.epam.reportportal.rules.commons.validation.Suppliers.formattedSupplier;
import static com.epam.reportportal.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static com.epam.ta.reportportal.commons.Predicates.equalTo;
import static com.epam.ta.reportportal.commons.Predicates.in;
import static com.epam.ta.reportportal.commons.Predicates.isNull;
import static com.epam.ta.reportportal.commons.Predicates.isPresent;
import static com.epam.ta.reportportal.commons.Predicates.not;
import static java.util.Optional.ofNullable;

import com.epam.reportportal.extension.CommonPluginCommand;
import com.epam.reportportal.extension.IntegrationGroupEnum;
import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.reportportal.extension.bugtracking.jira.api.model.CreatedIssue;
import com.epam.reportportal.extension.bugtracking.jira.api.model.IssueBean;
import com.epam.reportportal.extension.bugtracking.jira.api.model.IssueTypeDetails;
import com.epam.reportportal.extension.bugtracking.jira.api.model.IssueUpdateDetails;
import com.epam.reportportal.extension.bugtracking.jira.api.model.PageOfCreateMetaIssueTypeWithField;
import com.epam.reportportal.extension.bugtracking.jira.api.model.Project;
import com.epam.reportportal.extension.bugtracking.jira.api.model.ProjectComponent;
import com.epam.reportportal.extension.bugtracking.jira.api.model.SearchResults;
import com.epam.reportportal.extension.bugtracking.jira.api.model.Version;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraRestClient;
import com.epam.reportportal.extension.bugtracking.jira.utils.IssueField;
import com.epam.reportportal.model.externalsystem.AllowedValue;
import com.epam.reportportal.model.externalsystem.PostFormField;
import com.epam.reportportal.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.model.externalsystem.Ticket;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.AuthType;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import lombok.SneakyThrows;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.ByteArrayBody;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.jasypt.util.text.BasicTextEncryptor;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

/**
 * JIRA related implementation of {@link BtsExtension}.
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */

// TODO REVIEW this class after full JIRA support implementation
@Extension
@Component
public class JiraStrategy implements ReportPortalExtensionPoint, BtsExtension {

  private static final String DOCUMENTATION_LINK_FIELD = "documentationLink";
  private static final String DOCUMENTATION_LINK =
      "https://reportportal.io/docs/plugins/AtlassianJiraServer";

  private static final String NAME_FIELD = "name";

  private static final String PLUGIN_NAME = "Jira Server";


  private static final Logger LOGGER = LoggerFactory.getLogger(JiraStrategy.class);

  @Autowired
  @Qualifier("attachmentDataStoreService")
  private DataStoreService dataStoreService;

  @Autowired
  private BasicTextEncryptor basicTextEncryptor;

  @Autowired
  private LogRepository logRepository;

  @Autowired
  private TestItemRepository itemRepository;

  ObjectMapper objectMapper = configureObjectMapper();

  private Supplier<JIRATicketDescriptionService> descriptionService =
      Suppliers.memoize(() -> new JIRATicketDescriptionService(logRepository, itemRepository));

  @Override
  public Map<String, ?> getPluginParams() {
    Map<String, Object> params = new HashMap<>();
    params.put(DOCUMENTATION_LINK_FIELD, DOCUMENTATION_LINK);
    params.put(NAME_FIELD, PLUGIN_NAME);
    return params;
  }

  @Override
  public CommonPluginCommand getCommonCommand(String commandName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public PluginCommand getIntegrationCommand(String commandName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public IntegrationGroupEnum getIntegrationGroup() {
    return IntegrationGroupEnum.BTS;
  }

  protected ObjectMapper configureObjectMapper() {
    ObjectMapper om = new ObjectMapper();
    om.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
    om.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    om.registerModule(new JavaTimeModule());
    return om;
  }

  @Override
  public boolean testConnection(Integration system) {
    try {
      IntegrationParams params = ofNullable(system.getParams())
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Integration params are not specified."));

      String url = JiraProps.URL.getParam(params)
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
      String username = JiraProps.USER_NAME.getParam(params)
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Username is not specified."));
      String password = JiraProps.PASSWORD.getParam(params)
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Password is not specified."));
      String projectKey = JiraProps.PROJECT.getParam(params)
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Project is not specified."));

      validateExternalSystemDetails(system);
      var client = new JiraRestClient(url, username, basicTextEncryptor.decrypt(password));
      return client.projectsApi().getProject(projectKey, null, null) != null;
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return false;
    }
  }

  @Override
  //    @Cacheable(value = CacheConfiguration.EXTERNAL_SYSTEM_TICKET_CACHE, key = "#system.url + #system.project + #id")
  public Optional<Ticket> getTicket(final String id, Integration integration) {
    try {
      var client = getClient(integration.getParams());
      return getTicket(id, integration.getParams(), client);
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return Optional.empty();
    }
  }

  private Optional<Ticket> getTicket(String id, IntegrationParams details, JiraRestClient jiraRestClient) {
    SearchResults issues = findIssue(id, jiraRestClient);
    if (issues.getTotal() > 0) {
      IssueBean issue = jiraRestClient.issuesApi().getIssue(id, null, null, null, null, null, null);
      return Optional.of(JIRATicketUtils.toTicket(issue, JiraProps.URL.getParam(details)
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."))));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public Ticket submitTicket(final PostTicketRQ ticketRQ, Integration details) {

    expect(ticketRQ.getFields(), not(isNull()))
        .verify(UNABLE_INTERACT_WITH_INTEGRATION, "External System fields set is empty!");
    List<PostFormField> fields = ticketRQ.getFields();

    // TODO add validation of any field with allowedValues() array
    // Additional validation required for unsupported
    // ticket type and/or components in JIRA.
    PostFormField issueType = new PostFormField();
    PostFormField components = new PostFormField();
    for (PostFormField object : fields) {
      if ("issuetype".equalsIgnoreCase(object.getId())) {
        issueType = object;
      }
      if ("components".equalsIgnoreCase(object.getId())) {
        components = object;
      }
    }

    expect(issueType.getValue().size(), equalTo(1)).verify(UNABLE_INTERACT_WITH_INTEGRATION,
        formattedSupplier("[IssueType] field has multiple values '{}' but should be only one",
            issueType.getValue()
        )
    );
    final String issueTypeStr = issueType.getValue().get(0);
    try {
      JiraRestClient client = getClient(details.getParams());
      Project jiraProject = getProject(client, details);

      if (null != components.getValue()) {
        List<ProjectComponent> validComponents = jiraProject.getComponents();

        validComponents.forEach(component -> expect(component, in(validComponents))
            .verify(UNABLE_INTERACT_WITH_INTEGRATION, formattedSupplier("Component '{}' not exists in the external system", component)));
      }

      // TODO consider to modify code below - project cached
      IssueTypeDetails projectIssueType = jiraProject.getIssueTypes().stream()
          .filter(input -> issueTypeStr.equalsIgnoreCase(input.getName()))
          .findFirst()
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
              formattedSupplier("Unable post issue with type '{}' for project '{}'.", issueTypeStr, details.getProject())));

      IssueUpdateDetails issueRequest = JIRATicketUtils.toIssueInput(client, jiraProject, projectIssueType, ticketRQ, descriptionService.get());

      Map<String, String> binaryData = findBinaryData(issueRequest);

      /*
       * Claim because we want to be sure everything is OK
       */
      CreatedIssue createdIssue = client.issuesApi().createIssue(issueRequest, false);
      String issueKey = createdIssue.getKey();

      // post binary data
      if (!binaryData.isEmpty()) {
        addAttachment(issueKey, details, binaryData);
      }

      return getTicket(issueKey, details.getParams(), client)
          .orElse(null);

    } catch (ReportPortalException e) {
      throw e;
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      throw new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, e.getMessage());
    }
  }

  /**
   * Get jira's {@link Project} object.
   *
   * @param jiraRestClient Jira API client
   * @param system         Integration system
   * @return Jira Project
   */
  private Project getProject(JiraRestClient jiraRestClient, Integration system) {
    IntegrationParams params = ofNullable(system.getParams()).orElseThrow(
        () -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
            "Integration params are not specified."
        ));
    String projectKey = JiraProps.PROJECT.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Project is not specified."));
    return jiraRestClient.projectsApi().getProject(projectKey, null, null);
  }

  private SearchResults findIssue(String id, JiraRestClient jiraRestClient) {
    return jiraRestClient.issueSearchApi().searchForIssuesUsingJql("issue = " + id, null, 50, "", null, null, null, false, false);
  }

  /**
   * Parse ticket description and find binary data
   *
   * @param issueInput Jira issue
   * @return Parsed parameters
   */
  private Map<String, String> findBinaryData(IssueUpdateDetails issueInput) {
    Map<String, String> binary = new HashMap<>();
    if (issueInput.getFields().get(IssueField.DESCRIPTION_FIELD.getValue()) != null) {
      String description = issueInput.getFields().get(IssueField.DESCRIPTION_FIELD.getValue()).toString();
      // !54086a2c3c0c7d4446beb3e6.jpg| or [^54086a2c3c0c7d4446beb3e6.xml]
      String regex = "(!|\\[\\^)\\w+\\.\\w{0,10}(\\||\\])";
      Matcher matcher = Pattern.compile(regex).matcher(description);
      while (matcher.find()) {
        String rawValue = description.subSequence(matcher.start(), matcher.end()).toString();
        String binaryDataName =
            rawValue.replace("!", "").replace("[", "").replace("]", "").replace("^", "")
                .replace("|", "");
        String binaryDataId = binaryDataName.split("\\.")[0];
        binary.put(binaryDataId, binaryDataName);
      }
    }
    return binary;
  }

  @Override
  public List<PostFormField> getTicketFields(final String ticketType, Integration details) {
    List<PostFormField> result = new ArrayList<>();

    try {
      var client = getClient(details.getParams());
      Project jiraProject = getProject(client, details);

      IssueTypeDetails issueType = jiraProject.getIssueTypes().stream()
          .filter(input -> ticketType.equalsIgnoreCase(input.getName()))
          .findFirst()
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Issue type '" + ticketType + "' not found"));

      PageOfCreateMetaIssueTypeWithField issueCreateMetadata = client.issuesApi()
          .getCreateIssueMetaIssueTypeId(jiraProject.getId(), issueType.getId(), 0, 1000);

      ((List<Map<String, Object>>) issueCreateMetadata.getAdditionalProperties().get("values")).stream()
          .map(a -> objectMapper.convertValue(a, new TypeReference<Map<String, Object>>() {
          }))
          .map(field -> (JsonNode) new ObjectMapper().valueToTree(field))
          .forEach(jsonField -> {
                List<String> defValue = null;
                String fieldID = jsonField.get("fieldId").asText();
                String fieldName = jsonField.get("name").asText();
                String fieldType = jsonField.get("schema").get("type").asText();
                boolean required = jsonField.get("required").asBoolean();

                // Provide values for custom fields with predefined options
                List<AllowedValue> allowedList = new ArrayList<>();
                if (jsonField.get("allowedValues") != null) {
                  allowedList.addAll(StreamSupport.stream(jsonField.get("allowedValues").spliterator(), false)
                      .filter(JIRATicketUtils::isCustomField)
                      .map(allowedType -> new AllowedValue(allowedType.get("id").asText(), allowedType.get("value").asText()))
                      .toList());
                }

                if (fieldID.equalsIgnoreCase(COMPONENTS_FIELD.getValue())) {
                  for (ProjectComponent component : jiraProject.getComponents()) {
                    allowedList.add(new AllowedValue(component.getId(), component.getName()));
                  }
                }
                if (fieldID.equalsIgnoreCase(FIX_VERSIONS_FIELD.getValue())) {
                  for (Version version : jiraProject.getVersions()) {
                    allowedList.add(new AllowedValue(version.getId(), version.getName()));
                  }
                }
                if (fieldID.equalsIgnoreCase(AFFECTS_VERSIONS_FIELD.getValue())) {
                  for (Version version : jiraProject.getVersions()) {
                    allowedList.add(new AllowedValue(version.getId(), version.getName()));
                  }
                }
                if (fieldID.equalsIgnoreCase(PRIORITY_FIELD.getValue())) {
                  if (jsonField.get("allowedValues") != null) {
                    allowedList.addAll(StreamSupport.stream(jsonField.get("allowedValues").spliterator(), false)
                        .map(allowedType -> new AllowedValue(allowedType.get("id").asText(), allowedType.get("name").asText()))
                        .toList());
                  }
                }

                if (fieldID.equalsIgnoreCase(IssueField.ISSUE_TYPE_FIELD.getValue())) {
                  defValue = Collections.singletonList(ticketType);
                }

                //@formatter:off
                // Skip project field as external from list
                // Skip attachment cause we are not providing this functionality now
                // Skip timetracking field cause complexity. There are two fields with Original Estimation and Remaining Estimation.
                // Skip Story Link as greenhopper plugin field.
                // Skip Sprint field as complex one.
                //@formatter:on
                if ("project".equalsIgnoreCase(fieldID)
                    || "attachment".equalsIgnoreCase(fieldID)
                    || "timetracking".equalsIgnoreCase(fieldID)
                    || "Epic Link".equalsIgnoreCase(fieldName)
                    || "Sprint".equalsIgnoreCase(fieldName)) {
                  return;
                }

                result.add(new PostFormField(fieldID, fieldName, fieldType, required, defValue, allowedList));

              }
          );
      return result;
    } catch (Exception e) {
      LOGGER.error(e.getMessage(), e);
      return new ArrayList<>();
    }

  }

  @Override
  public List<String> getIssueTypes(Integration system) {
    try {
      JiraRestClient client = getClient(system.getParams());
      String projectKey = JiraProps.PROJECT.getParam(system.getParams())
          .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Project is not specified."));
      Project jiraProject = client.projectsApi().getProject(projectKey, null, null);

      return jiraProject.getIssueTypes().stream()
          .map(IssueTypeDetails::getName)
          .toList();
    } catch (RestClientException e) {
      throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Project not found.");
    } catch (Exception e) {
      throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Check connection settings.");
    }
  }

  /**
   * JIRA properties validator
   *
   * @param details External system details
   */
  private void validateExternalSystemDetails(Integration details) {
    AuthType authType = AuthType.findByName(JiraProps.AUTH_TYPE.getParam(details.getParams())
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION,
            "Auth type value cannot be NULL"
        ))).orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_AUTHENTICATION_TYPE));
    if (AuthType.BASIC.equals(authType)) {
      expect(JiraProps.USER_NAME.getParam(details.getParams()), isPresent()).verify(
          UNABLE_INTERACT_WITH_INTEGRATION, "Username value cannot be NULL");
      expect(JiraProps.PASSWORD.getParam(details.getParams()), isPresent()).verify(
          UNABLE_INTERACT_WITH_INTEGRATION, "Password value cannot be NULL");
    } else if (AuthType.OAUTH.equals(authType)) {
      expect(JiraProps.OAUTH_ACCESS_KEY.getParam(details.getParams()), isPresent()).verify(
          UNABLE_INTERACT_WITH_INTEGRATION, "AccessKey value cannot be NULL");
    }
    expect(JiraProps.PROJECT.getParam(details.getParams()), isPresent()).verify(
        UNABLE_INTERACT_WITH_INTEGRATION, "JIRA project value cannot be NULL");
    expect(JiraProps.URL.getParam(details.getParams()), isPresent()).verify(
        UNABLE_INTERACT_WITH_INTEGRATION, "JIRA URL value cannot be NULL");
  }


  public JiraRestClient getClient(IntegrationParams params) {
    String url = JiraProps.URL.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
    String username = JiraProps.USER_NAME.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Username is not specified."));
    String password = JiraProps.PASSWORD.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Password is not specified."));
    return new JiraRestClient(url, username, basicTextEncryptor.decrypt(password));
  }


  @SneakyThrows
  public void addAttachment(String issueKey, Integration integration, Map<String, String> binaryData) throws RestClientException {
    String url = JiraProps.URL.getParam(integration.getParams())
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
    String username = JiraProps.USER_NAME.getParam(integration.getParams())
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Username is not specified."));
    String password = JiraProps.PASSWORD.getParam(integration.getParams())
        .map(basicTextEncryptor::decrypt)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Password is not specified."));

    var count = 0;
    MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create()
        .setLaxMode()
        .setCharset(StandardCharsets.UTF_8);

    for (Map.Entry<String, String> binaryDataEntry : binaryData.entrySet()) {
      Optional<InputStream> data = dataStoreService.load(binaryDataEntry.getKey());
      if (data.isPresent()) {
        var fileName = binaryDataEntry.getValue();
        var bytes = IOUtils.toByteArray(data.get());
        if (bytes.length == 0) {
          LOGGER.warn("Empty file {}", fileName);
          continue;
        }
        ByteArrayBody fileBody = new ByteArrayBody(bytes, fileName);

        entityBuilder.addPart("file", fileBody);
        count++;
      }
      if (count > 0) {
        HttpPost request = new HttpPost(url + String.format("/rest/api/latest/issue/%s/attachments", issueKey));
        request.setEntity(entityBuilder.build());
        request.setHeader("X-Atlassian-Token", "no-check");
        request.setHeader("Authorization", "Basic " + getAuthorizationHeader(username, password));

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
          httpclient.execute(request, httpResponse -> {
            if (failedHttpResponse(httpResponse)) {
              LOGGER.error("{} {}", httpResponse.getCode(), httpResponse.getReasonPhrase());
              throw new ReportPortalException("Failed to upload attachment for " + issueKey);
            }
            return httpResponse;
          });
        }
      }
    }
  }

  public static String getAuthorizationHeader(String user, String password) {
    String plainCreds = user + ":" + password;
    byte[] base64CredsBytes = Base64.encodeBase64(plainCreds.getBytes(StandardCharsets.UTF_8));
    return new String(base64CredsBytes);
  }

  public static boolean failedHttpResponse(ClassicHttpResponse responseCode) {
    return !(responseCode.getCode() >= 200 && responseCode.getCode() < 300);
  }

}
