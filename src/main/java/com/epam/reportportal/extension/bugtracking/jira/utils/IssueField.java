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

package com.epam.reportportal.extension.bugtracking.jira.utils;


import com.epam.reportportal.rules.exception.ErrorType;
import com.epam.reportportal.rules.exception.ReportPortalException;
import java.util.Arrays;

public enum IssueField {
  AFFECTS_VERSIONS_FIELD("versions"),
  ASSIGNEE_FIELD("assignee"),
  ATTACHMENT_FIELD("attachment"),
  COMMENT_FIELD("comment"),
  COMPONENTS_FIELD("components"),
  CREATED_FIELD("created"),
  DESCRIPTION_FIELD("description"),
  DUE_DATE_FIELD("duedate"),
  FIX_VERSIONS_FIELD("fixVersions"),
  ISSUE_TYPE_FIELD("issuetype"),
  LABELS_FIELD("labels"),
  LINKS_FIELD("issuelinks"),
  LINKS_PRE_5_0_FIELD("links"),
  PARENT_FIELD("parent"),
  PRIORITY_FIELD("priority"),
  PROJECT_FIELD("project"),
  REPORTER_FIELD("reporter"),
  RESOLUTION_FIELD("resolution"),
  STATUS_FIELD("status"),
  SUBTASKS_FIELD("subtasks"),
  SUMMARY_FIELD("summary"),
  TIMETRACKING_FIELD("timetracking"),
  TRANSITIONS_FIELD("transitions"),
  UPDATED_FIELD("updated"),
  VOTES_FIELD("votes"),
  WATCHER_FIELD("watches"),
  WATCHER_PRE_5_0_FIELD("watcher"),
  WORKLOG_FIELD("worklog"),
  WORKLOGS_FIELD("worklogs");

  public final String value;

  IssueField(String value) {
    this.value = value;
  }

  public static IssueField fromString(String mode) {
    return Arrays.stream(IssueField.values())
        .filter(it -> it.getValue().equalsIgnoreCase(mode))
        .findFirst()
        .orElseThrow(() -> new ReportPortalException(
            ErrorType.INCORRECT_REQUEST,
            "Incorrect analyze items mode. Allowed are: " + Arrays.stream(IssueField.values())
                .map(IssueField::getValue)
                .toList()
        ));
  }

  public String getValue() {
    return value;
  }
}
