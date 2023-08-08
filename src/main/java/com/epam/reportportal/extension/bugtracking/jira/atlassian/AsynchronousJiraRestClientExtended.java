package com.epam.reportportal.extension.bugtracking.jira.atlassian;

import com.atlassian.jira.rest.client.api.*;
import com.atlassian.jira.rest.client.internal.async.*;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URI;

/**
 * Temporary solution, should be removed after jira-rest-java-client-core update.
 */
public class AsynchronousJiraRestClientExtended implements JiraRestClient {

	private final IssueRestClient issueRestClient;
	private final SessionRestClient sessionRestClient;
	private final UserRestClient userRestClient;
	private final GroupRestClient groupRestClient;
	private final ProjectRestClient projectRestClient;
	private final ComponentRestClient componentRestClient;
	private final MetadataRestClient metadataRestClient;
	private final SearchRestClient searchRestClient;
	private final VersionRestClient versionRestClient;
	private final ProjectRolesRestClient projectRolesRestClient;
	private final MyPermissionsRestClient myPermissionsRestClient;
	private final DisposableHttpClient httpClient;
	private final AuditRestClient auditRestClient;

	public AsynchronousJiraRestClientExtended(final URI serverUri, final DisposableHttpClient httpClient) {
		final URI baseUri = UriBuilder.fromUri(serverUri).path("/rest/api/latest").build();

		this.httpClient = httpClient;
		metadataRestClient = new AsynchronousMetadataRestClient(baseUri, httpClient);
		sessionRestClient = new AsynchronousSessionRestClient(serverUri, httpClient);
		issueRestClient = new AsynchronousIssueRestClientExtended(baseUri, httpClient, sessionRestClient, metadataRestClient);
		userRestClient = new AsynchronousUserRestClient(baseUri, httpClient);
		groupRestClient  = new AsynchronousGroupRestClient(baseUri, httpClient);
		projectRestClient = new AsynchronousProjectRestClient(baseUri, httpClient);
		componentRestClient = new AsynchronousComponentRestClient(baseUri, httpClient);
		searchRestClient = new AsynchronousSearchRestClient(baseUri, httpClient);
		versionRestClient = new AsynchronousVersionRestClient(baseUri, httpClient);
		projectRolesRestClient = new AsynchronousProjectRolesRestClient(serverUri, httpClient);
		myPermissionsRestClient = null;
		auditRestClient = null;
	}

	@Override
	public IssueRestClient getIssueClient() {
		return issueRestClient;
	}

	@Override
	public SessionRestClient getSessionClient() {
		return sessionRestClient;
	}

	@Override
	public UserRestClient getUserClient() {
		return userRestClient;
	}

	@Override
	public GroupRestClient getGroupClient() {
		return groupRestClient;
	}

	@Override
	public ProjectRestClient getProjectClient() {
		return projectRestClient;
	}

	@Override
	public ComponentRestClient getComponentClient() {
		return componentRestClient;
	}

	@Override
	public MetadataRestClient getMetadataClient() {
		return metadataRestClient;
	}

	@Override
	public SearchRestClient getSearchClient() {
		return searchRestClient;
	}

	@Override
	public VersionRestClient getVersionRestClient() {
		return versionRestClient;
	}

	@Override
	public ProjectRolesRestClient getProjectRolesRestClient() {
		return projectRolesRestClient;
	}

	@Override
	public MyPermissionsRestClient getMyPermissionsRestClient() {
		return myPermissionsRestClient;
	}

	@Override
	public AuditRestClient getAuditRestClient() {
		return auditRestClient;
	}

	@Override
	public void close() throws IOException {
		try {
			httpClient.destroy();
		} catch (Exception e) {
			throw (e instanceof IOException) ? ((IOException) e) : new IOException(e);
		}
	}
}