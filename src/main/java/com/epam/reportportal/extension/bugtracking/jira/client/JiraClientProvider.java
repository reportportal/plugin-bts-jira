package com.epam.reportportal.extension.bugtracking.jira.client;

import static com.epam.reportportal.base.infrastructure.rules.exception.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;

import com.epam.reportportal.base.infrastructure.persistence.entity.integration.IntegrationParams;
import com.epam.reportportal.base.infrastructure.rules.exception.ReportPortalException;
import com.epam.reportportal.extension.bugtracking.jira.JiraProps;
import org.jasypt.util.text.BasicTextEncryptor;

public class JiraClientProvider {

  private final BasicTextEncryptor textEncryptor;

  public JiraClientProvider(BasicTextEncryptor textEncryptor) {
    this.textEncryptor = textEncryptor;
  }

  public JiraRestClient provide(IntegrationParams params) {
    String url = JiraProps.URL.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
    String username = JiraProps.USER_NAME.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Username is not specified."));
    String password = JiraProps.PASSWORD.getParam(params)
        .orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Password is not specified."));
    return new JiraRestClient(url, username, textEncryptor.decrypt(password));
  }
}
