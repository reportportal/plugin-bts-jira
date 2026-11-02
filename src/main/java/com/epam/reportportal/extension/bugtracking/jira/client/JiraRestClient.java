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

package com.epam.reportportal.extension.bugtracking.jira.client;

import com.epam.reportportal.extension.bugtracking.jira.api.IssueAttachmentsApi;
import com.epam.reportportal.extension.bugtracking.jira.api.IssueLinksApi;
import com.epam.reportportal.extension.bugtracking.jira.api.IssueSearchApi;
import com.epam.reportportal.extension.bugtracking.jira.api.IssuesApi;
import com.epam.reportportal.extension.bugtracking.jira.api.ProjectsApi;
import com.epam.reportportal.extension.bugtracking.jira.api.UserSearchApi;
import com.epam.reportportal.extension.bugtracking.jira.api.UsersApi;
import com.epam.reportportal.extension.bugtracking.jira.api.client.ApiClient;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.RestClient;

@Slf4j
@Getter
public class JiraRestClient {

  private final ApiClient apiClient;

  public JiraRestClient(String url, String username, String apikey) {
    boolean debugging = Boolean.parseBoolean(System.getenv().getOrDefault("DEBUGGER_ENABLED", "false"));

    this.apiClient = new ApiClient(buildRestClient(debugging));
    this.apiClient.setBasePath(url);
    this.apiClient.setUsername(username);
    this.apiClient.setPassword(apikey);
  }

  private static RestClient buildRestClient(boolean debugging) {
    RestClient.Builder builder = ApiClient.buildRestClientBuilder();
    if (debugging) {
      builder.requestInterceptor((request, body, execution) -> {
        var response = execution.execute(request, body);
        log.info("Jira request: {} {} -> {}", request.getMethod(), request.getURI(), response.getStatusCode());
        return response;
      });
    }
    return builder.build();
  }

  public ProjectsApi projectsApi() {
    return new ProjectsApi(apiClient);
  }

  public UsersApi usersApi() {
    return new UsersApi(apiClient);
  }

  public UserSearchApi userSearchApi() {
    return new UserSearchApi(apiClient);
  }

  public IssuesApi issuesApi() {
    return new IssuesApi(apiClient);
  }

  public IssueSearchApi issueSearchApi() {
    return new IssueSearchApi(apiClient);
  }

  public IssueLinksApi issueLinksApi() {
    return new IssueLinksApi(apiClient);
  }

  public IssueAttachmentsApi issueAttachmentsApi() {
    return new IssueAttachmentsApi(apiClient);
  }

}
