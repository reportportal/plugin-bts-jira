/*
 * Copyright 2021 EPAM Systems
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

import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

/**
 * @author <a href="mailto:pavel_bortnik@epam.com">Pavel Bortnik</a>
 */
@Getter
public enum CloudJiraProperties {

	EMAIL("email"),
	PROJECT("project"),
	API_TOKEN("apiToken"),
	URL("url");

	private final String name;

	CloudJiraProperties(String name) {
		this.name = name;
	}

	public Optional<String> getParam(Map<String, Object> params) {
		return Optional.ofNullable(params.get(this.name)).map(String::valueOf);
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
