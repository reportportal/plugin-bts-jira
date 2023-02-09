package com.epam.reportportal.extension.bugtracking.jira.atlassian;

import com.atlassian.httpclient.api.HttpClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.api.domain.CimFieldInfo;
import com.atlassian.jira.rest.client.api.domain.Page;
import com.atlassian.jira.rest.client.internal.async.AsynchronousIssueRestClient;
import io.atlassian.util.concurrent.Promise;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

/**
 * Temporary solution, should be removed after jira-rest-java-client-core update.
 */
public class AsynchronousIssueRestClientExtended extends AsynchronousIssueRestClient {

	private final URI baseUri;

	public AsynchronousIssueRestClientExtended(URI baseUri, HttpClient client, SessionRestClient sessionRestClient, MetadataRestClient metadataRestClient) {
		super(baseUri, client, sessionRestClient, metadataRestClient);
		this.baseUri = baseUri;
	}

	@Override
	public Promise<Page<CimFieldInfo>> getCreateIssueMetaFields(@Nonnull final String projectIdOrKey, @Nonnull final String issueTypeId,
			@Nullable final Long startAt, @Nullable final Integer maxResults) {
		final UriBuilder uriBuilder = UriBuilder.fromUri(baseUri).path("issue/createmeta/" + projectIdOrKey + "/issuetypes/" + issueTypeId);

		return getAndParse(uriBuilder.build(), new CreateIssueMetaFieldsParserExt());
	}
}
