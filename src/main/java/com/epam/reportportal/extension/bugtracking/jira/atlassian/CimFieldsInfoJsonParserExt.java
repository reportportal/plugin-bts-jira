package com.epam.reportportal.extension.bugtracking.jira.atlassian;

import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.internal.json.CimFieldsInfoJsonParser;
import com.atlassian.jira.rest.client.internal.json.JsonParseUtil;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

/**
 * Temporary solution, should be removed after jira-rest-java-client-core update.
 */
public class CimFieldsInfoJsonParserExt extends CimFieldsInfoJsonParser {

    @Override
    public CimFieldInfo parse(JSONObject json) throws JSONException {
        final String id = JsonParseUtil.getOptionalString(json, "fieldId");
        return parse(json, id);
    }
}
