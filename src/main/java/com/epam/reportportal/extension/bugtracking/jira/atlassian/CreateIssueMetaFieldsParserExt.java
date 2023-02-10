package com.epam.reportportal.extension.bugtracking.jira.atlassian;

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.Page;
import com.atlassian.jira.rest.client.internal.json.CreateIssueMetaFieldsParser;
import com.atlassian.jira.rest.client.internal.json.GenericJsonArrayParser;
import com.atlassian.jira.rest.client.internal.json.PageJsonParser;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Temporary solution, should be removed after jira-rest-java-client-core update.
 */
public class CreateIssueMetaFieldsParserExt extends CreateIssueMetaFieldsParser {

    private final PageJsonParser<CimFieldInfo> pageParser = new PageJsonParser<>(new GenericJsonArrayParser<CimFieldInfo>(new CimFieldsInfoJsonParserExt()));

    @Override
    public Page<CimFieldInfo> parse(JSONObject json) throws JSONException {
        return pageParser.parse(json);
    }
}
