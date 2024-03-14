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

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.CustomFieldOption;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Page;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.User;
import com.atlassian.jira.rest.client.api.domain.input.ComplexIssueInputFieldValue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.epam.ta.reportportal.commons.Predicates;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.commons.validation.Suppliers;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import com.epam.ta.reportportal.ws.reporting.ErrorType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
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
  public static final DateTimeFormatter JIRA_DATE_FORMATTER = ISODateTimeFormat.date();

  // Field format from UI calendar control
  public static final String JIRA_FORMAT = "yyyy-MM-dd";

  private JIRATicketUtils() {
  }

  public static Ticket toTicket(Issue input, String jiraUrl) {
    Ticket ticket = new Ticket();
    ticket.setId(input.getKey());
    ticket.setSummary(input.getSummary());
    ticket.setStatus(input.getStatus().getName());
    ticket.setTicketUrl(stripEnd(jiraUrl, "/") + "/browse/" + input.getKey());
    return ticket;
  }

  public static IssueInput toIssueInput(JiraRestClient client, Project jiraProject,
      Optional<IssueType> issueType, PostTicketRQ ticketRQ,
      JIRATicketDescriptionService descriptionService) {
    String userDefinedDescription = "";
    IssueInputBuilder issueInputBuilder = new IssueInputBuilder(jiraProject, issueType.get());
    Page<CimFieldInfo> fieldsInfoPage = client.getIssueClient()
        .getCreateIssueMetaFields(jiraProject.getKey(), issueType.get().getId().toString(), null,
            null
        ).claim();
    BusinessRule.expect(fieldsInfoPage.getValues().iterator().hasNext(), Predicates.equalTo(true))
        .verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            String.format("Issue fields %s not found", issueType.get().getId().toString())
        );
    Map<String, CimFieldInfo> fieldsInfo = Streams.stream(fieldsInfoPage.getValues())
        .collect(Collectors.toMap(CimFieldInfo::getId, Function.identity()));
    List<PostFormField> fields = ticketRQ.getFields();
    for (PostFormField one : fields) {
      CimFieldInfo cimFieldInfo = fieldsInfo.get(one.getId());
      if (one.getIsRequired() && CollectionUtils.isEmpty(one.getValue())) {
        BusinessRule.fail().withError(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
            Suppliers.formattedSupplier("Required parameter '{}' is empty", one.getFieldName())
        );
      }

      if (!checkField(one)) {
        continue;
      }

      // Skip issuetype and project fields cause got them in
      // issueInputBuilder already
      if (one.getId().equalsIgnoreCase(IssueFieldId.ISSUE_TYPE_FIELD.id) || one.getId()
          .equalsIgnoreCase(IssueFieldId.PROJECT_FIELD.id)) {
        continue;
      }

      if (one.getId().equalsIgnoreCase(IssueFieldId.DESCRIPTION_FIELD.id)) {
        userDefinedDescription = one.getValue().get(0);
      }
      if (one.getId().equalsIgnoreCase(IssueFieldId.SUMMARY_FIELD.id)) {
        issueInputBuilder.setSummary(one.getValue().get(0));
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueFieldId.PRIORITY_FIELD.id)) {
        if (null != IssuePriority.findByName(one.getValue().get(0))) {
          issueInputBuilder.setPriorityId(
              IssuePriority.findByName(one.getValue().get(0)).getValue());
        }
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueFieldId.COMPONENTS_FIELD.id)) {
        issueInputBuilder.setComponentsNames(one.getValue());
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueFieldId.ASSIGNEE_FIELD.id)) {
        issueInputBuilder.setAssigneeName(one.getValue().get(0));
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueFieldId.REPORTER_FIELD.id)) {
        issueInputBuilder.setReporterName(one.getValue().get(0));
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueFieldId.AFFECTS_VERSIONS_FIELD.id)) {
        issueInputBuilder.setAffectedVersionsNames(one.getValue());
        continue;
      }
      if (one.getId().equalsIgnoreCase(IssueFieldId.FIX_VERSIONS_FIELD.id)) {
        issueInputBuilder.setFixVersionsNames(one.getValue());
        continue;
      }

      // Arrays and fields with 'allowedValues' handler
      if (null != cimFieldInfo.getAllowedValues()) {
        try {
          List<ComplexIssueInputFieldValue> arrayOfValues = Lists.newArrayList();
          for (Object o : cimFieldInfo.getAllowedValues()) {
            if (o instanceof CustomFieldOption) {
              CustomFieldOption cfo = (CustomFieldOption) o;
              if (one.getValue().contains(cfo.getValue())) {
                arrayOfValues.add(
                    ComplexIssueInputFieldValue.with("id", String.valueOf(cfo.getId())));
              }
            }
          }
          if (one.getFieldType().equalsIgnoreCase(IssueFieldType.ARRAY.name)) {
            issueInputBuilder.setFieldValue(one.getId(), arrayOfValues);
          } else {
            issueInputBuilder.setFieldValue(one.getId(), arrayOfValues.get(0));
          }
        } catch (Exception e) {
          LOGGER.error(e.getMessage(), e);
          issueInputBuilder.setFieldValue(one.getId(), "ReportPortal autofield");
        }
      } else {
        if (one.getFieldType().equalsIgnoreCase(IssueFieldType.ARRAY.name)) {
          if (one.getId().equalsIgnoreCase(IssueFieldId.LABELS_FIELD.id)) {
            issueInputBuilder.setFieldValue(one.getId(), processLabels(one.getValue().get(0)));
          } else {
            issueInputBuilder.setFieldValue(one.getId(), one.getValue());
          }
        } else if (one.getFieldType().equalsIgnoreCase(IssueFieldType.NUMBER.name)) {
          issueInputBuilder.setFieldValue(one.getId(), Long.valueOf(one.getValue().get(0)));
        } else if (one.getFieldType().equalsIgnoreCase(IssueFieldType.USER.name)) {
          if (!one.getValue().get(0).equals("")) {
            // TODO create user cache (like for projects) for JIRA
            // 'user' type fields
            User jiraUser = client.getUserClient().getUser(one.getValue().get(0)).claim();
            // FIXME change validator as common validate method for
            // fields
            BusinessRule.expect(jiraUser, Predicates.notNull())
                .verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, Suppliers.formattedSupplier(
                    "Value for '{}' field with 'user' type wasn't found in JIRA",
                    one.getValue().get(0)
                ));
            issueInputBuilder.setFieldValue(one.getId(), jiraUser);
          }
        } else if (one.getFieldType().equalsIgnoreCase(IssueFieldType.DATE.name)) {
          try {
            SimpleDateFormat format = new SimpleDateFormat(JIRA_FORMAT);
            Date fieldValue = format.parse(one.getValue().get(0));
            issueInputBuilder.setFieldValue(
                one.getId(), JIRA_DATE_FORMATTER.print(fieldValue.getTime()));
          } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
          }
        } else {
          issueInputBuilder.setFieldValue(one.getId(), one.getValue().get(0));
        }
      }
    }
    issueInputBuilder.setDescription(
        userDefinedDescription.concat("\n").concat(descriptionService.getDescription(ticketRQ)));
    return issueInputBuilder.build();
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
}