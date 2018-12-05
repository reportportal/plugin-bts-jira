package com.epam.reportportal.extension.bugtracking.jira;

import com.epam.ta.reportportal.entity.integration.IntegrationParams;

import java.util.HashMap;
import java.util.Optional;

public enum JiraProps {

	USER_NAME("username"),
	PASSWORD("password"),
	PROJECT("project"),
	AUTH_TYPE("authType"),
	OAUTH_ACCESS_KEY("oauthAccessKey"),
	URL("url");

	private final String name;

	JiraProps(String name) {
		this.name = name;
	}

	public Optional<String> getParam(IntegrationParams params) {
		return Optional.ofNullable(params.getParams().get(this.name)).map(o -> (String) o);
	}

	public void setParam(IntegrationParams params, String value) {
		if (null == params.getParams()) {
			params.setParams(new HashMap<>());
		}
		params.getParams().put(this.name, value);
	}
}
