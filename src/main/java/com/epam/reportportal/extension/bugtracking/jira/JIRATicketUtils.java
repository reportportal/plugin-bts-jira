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

import static com.epam.reportportal.extension.bugtracking.jira.utils.IssueField.ASSIGNEE_FIELD;

import com.epam.reportportal.extension.bugtracking.jira.api.model.EntityProperty;
import com.epam.reportportal.extension.bugtracking.jira.api.model.IssueBean;
import com.epam.reportportal.extension.bugtracking.jira.api.model.IssueTypeDetails;
import com.epam.reportportal.extension.bugtracking.jira.api.model.IssueUpdateDetails;
import com.epam.reportportal.extension.bugtracking.jira.api.model.PageOfCreateMetaIssueTypeWithField;
import com.epam.reportportal.extension.bugtracking.jira.api.model.Project;
import com.epam.reportportal.extension.bugtracking.jira.api.model.User;
import com.epam.reportportal.extension.bugtracking.jira.client.JiraRestClient;
import com.epam.reportportal.extension.bugtracking.jira.utils.IssueField;
import com.epam.reportportal.model.externalsystem.PostFormField;
import com.epam.reportportal.model.externalsystem.PostTicketRQ;
import com.epam.reportportal.model.externalsystem.Ticket;
import com.epam.reportportal.rules.commons.validation.BusinessRule;
import com.epam.reportportal.rules.commons.validation.Suppliers;
import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.ta.reportportal.commons.Predicates;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide toolscom.epam.reportportal.extension.bugtracking.jira for working with JIRA tickets(conversion).
 *
 * @author Aliaksei_Makayed
 * @author Andrei_Ramanchuk
 */
public class JIRATicketUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(JIRATicketUtils.class);

  // Field format from UI calendar control
  public static final String JIRA_FORMAT = "yyyy-MM-dd";

  private JIRATicketUtils() {
  }

  public static Ticket toTicket(IssueBean jiraIssue, String jiraUrl) {
    Ticket ticket = new Ticket();
    JsonNode jn = new ObjectMapper().valueToTree(jiraIssue);

    ticket.setId(jiraIssue.getKey());
    ticket.setSummary(jn.get("fields").get("summary").asText());
    ticket.setStatus(jn.get("fields").get("status").get("statusCategory").get("name").asText());
    ticket.setTicketUrl(stripEnd(jiraUrl, "/") + "/browse/" + jn.get("key").asText());
    return ticket;
  }

  public static IssueUpdateDetails toIssueInput(JiraRestClient client, Project jiraProject, IssueTypeDetails issueType, PostTicketRQ ticketRQ,
      JIRATicketDescriptionService descriptionService) {
    String userDefinedDescription = "";
    IssueUpdateDetails issueUpdateDetails = new IssueUpdateDetails();

    issueUpdateDetails.putFieldsItem("project", Map.entry("id", jiraProject.getId()));
    issueUpdateDetails.putFieldsItem("issuetype", Map.entry("id", issueType.getId()));

    PageOfCreateMetaIssueTypeWithField metaIssueTypePage = client.issuesApi()
        .getCreateIssueMetaIssueTypeId(jiraProject.getId(), issueType.getId(), 0, 100);
    var issueMetaFields = (List<Map<String, Object>>) metaIssueTypePage.getAdditionalProperties().get("values");
    var fieldsMap = issueMetaFields.stream().collect(Collectors.toMap(k -> k.get("fieldId"), Function.identity()));

    for (PostFormField one : ticketRQ.getFields()) {

      if (one.getIsRequired() && CollectionUtils.isEmpty(one.getValue())) {
        BusinessRule.fail()
            .withError(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
                Suppliers.formattedSupplier("Required parameter '{}' is empty", one.getFieldName()));
      }

      if (!checkField(one)) {
        continue;
      }

      // Skip issuetype and project fields cause got them in
      // issueInputBuilder already
      if (one.getId().equalsIgnoreCase(IssueField.ISSUE_TYPE_FIELD.value) || one.getId().equalsIgnoreCase(IssueField.PROJECT_FIELD.value)) {
        continue;
      }

      if (one.getId().equalsIgnoreCase(IssueField.DESCRIPTION_FIELD.getValue())) {
        userDefinedDescription = one.getValue().get(0);
      }
      if (one.getId().equalsIgnoreCase(IssueField.SUMMARY_FIELD.value)) {
        issueUpdateDetails.putFieldsItem(IssueField.SUMMARY_FIELD.value, one.getValue().get(0));
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueField.PRIORITY_FIELD.value)) {
        if (IssuePriority.findByName(one.getValue().get(0)) != null) {
          issueUpdateDetails.putFieldsItem(IssueField.PRIORITY_FIELD.value, one.getValue().get(0));
        }
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueField.COMPONENTS_FIELD.value)) {
        var names = one.getValue().stream().map(version -> Map.entry("name", version)).toList();
        issueUpdateDetails.putFieldsItem(IssueField.COMPONENTS_FIELD.getValue(), names);
        continue;
      }
      if (one.getId().equalsIgnoreCase(ASSIGNEE_FIELD.getValue())) {
        issueUpdateDetails.putFieldsItem(ASSIGNEE_FIELD.getValue(), Map.entry("name", one.getValue().get(0)));
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueField.REPORTER_FIELD.value)) {
        issueUpdateDetails.putFieldsItem(IssueField.REPORTER_FIELD.value, Map.entry("name", one.getValue().get(0)));
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueField.AFFECTS_VERSIONS_FIELD.value)) {
        var versions = one.getValue().stream().map(version -> Map.entry("name", version)).toList();
        issueUpdateDetails.putFieldsItem(IssueField.AFFECTS_VERSIONS_FIELD.value, versions);
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueField.FIX_VERSIONS_FIELD.value)) {
        var versions = one.getValue().stream().map(version -> Map.entry("name", version)).toList();
        issueUpdateDetails.putFieldsItem(IssueField.FIX_VERSIONS_FIELD.value, versions);
        continue;
      }

      var cimFieldInfo = fieldsMap.get(one.getId());
      // Arrays and fields with 'allowedValues' handler

      if (cimFieldInfo.get("allowedValues") != null) {
        try {
          var allowedValues = ((List<Map<String, Object>>)cimFieldInfo.get("allowedValues"));
          List<Object> arrayOfValues = new ArrayList<>();
          for (Object o : allowedValues) {
            JsonNode jn = new ObjectMapper().valueToTree(o);
            if (isCustomField(jn) && one.getValue().contains(jn.get("value").asText())) {
              arrayOfValues.add(Map.entry("id", jn.get("id").asText()));
            }
          }
          if (one.getFieldType().equalsIgnoreCase(IssueFieldType.ARRAY.name)) {
            issueUpdateDetails.putFieldsItem(one.getId(), arrayOfValues);
          } else {
            issueUpdateDetails.putFieldsItem(one.getId(), arrayOfValues.get(0));
          }
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
          issueUpdateDetails.putFieldsItem(one.getId(), "ReportPortal autofield");
        }
      } else {
        if (one.getFieldType().equalsIgnoreCase(IssueFieldType.ARRAY.name)) {
          if (one.getId().equalsIgnoreCase(IssueField.LABELS_FIELD.value)) {
            issueUpdateDetails.putFieldsItem(one.getId(), processLabels(one.getValue().get(0)));
          } else {
            issueUpdateDetails.putFieldsItem(one.getId(), one.getValue());
          }
        } else if (one.getFieldType().equalsIgnoreCase(IssueFieldType.NUMBER.name)) {
          issueUpdateDetails.putFieldsItem(one.getId(), Long.valueOf(one.getValue().get(0)));
        } else if (one.getFieldType().equalsIgnoreCase(IssueFieldType.USER.name)) {
          if (!one.getValue().get(0).equals("")) {
            // TODO create user cache (like for projects) for JIRA
            // 'user' type fields
            User jiraUser = client.usersApi().getUser(null, one.getValue().get(0), null, null);
            // FIXME change validator as common validate method for
            // fields
            BusinessRule.expect(jiraUser, Predicates.notNull())
                .verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, Suppliers.formattedSupplier(
                    "Value for '{}' field with 'user' type wasn't found in JIRA",
                    one.getValue().get(0)
                ));
            issueUpdateDetails.putFieldsItem(ASSIGNEE_FIELD.getValue(), Map.entry("id", jiraUser.getAccountId()));
          }
        } else if (one.getFieldType().equalsIgnoreCase(IssueFieldType.DATE.name)) {
          try {
            SimpleDateFormat format = new SimpleDateFormat(JIRA_FORMAT);
            Date fieldValue = format.parse(one.getValue().get(0));
            issueUpdateDetails.putFieldsItem(one.getId(), fieldValue.toInstant());
          } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
          }
        } else {
          issueUpdateDetails.putFieldsItem(one.getId(), one.getValue().get(0));
        }
      }
    }
    issueUpdateDetails.getProperties()
        .add(new EntityProperty("description", userDefinedDescription.concat("\n").concat(descriptionService.getDescription(ticketRQ))));
    return issueUpdateDetails;
  }

  /**
   * Processing labels for JIRA through spaces split
   *
   * @param values
   * @return
   */
  private static List<String> processLabels(String values) {
    return Stream.of(values.split(" ")).collect(Collectors.toList());
  }

  /**
   * Just JIRA field types enumerator
   *
   * @author Andrei_Ramanchuk
   */
  public enum IssueFieldType {
    //@formatter:off
		ARRAY("array"), 
		DATE("date"), 
		NUMBER("number"), 
		USER("user"),
		OPTION("option"),
		STRING("string");
		//@formatter:on

    private final String name;

    public String getName() {
      return name;
    }

    IssueFieldType(String value) {
      this.name = value;
    }

  }

  private static boolean checkField(PostFormField field) {
    return ((null != field.getValue()) && (!field.getValue().isEmpty()) && (!"".equals(
        field.getValue().get(0))));
  }

  @VisibleForTesting
  static String stripEnd(String str, String suffix) {
    String trimmed = str;
    if (str.endsWith(suffix)) {
      trimmed = str.substring(0, str.length() - suffix.length());
    }
    return trimmed;
  }

  public static boolean isCustomField(JsonNode allowedValue) {
    return allowedValue.get("self").asText().contains("/customFieldOption/");
  }
}
