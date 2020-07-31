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

import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptions;
import com.atlassian.jira.rest.client.api.GetCreateIssueMetadataOptionsBuilder;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.*;
import com.atlassian.jira.rest.client.api.domain.input.AttachmentInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.epam.reportportal.extension.IntegrationGroupEnum;
import com.epam.reportportal.extension.PluginCommand;
import com.epam.reportportal.extension.ReportPortalExtensionPoint;
import com.epam.reportportal.extension.bugtracking.BtsExtension;
import com.epam.ta.reportportal.binary.DataStoreService;
import com.epam.ta.reportportal.commons.Preconditions;
import com.epam.ta.reportportal.commons.validation.BusinessRule;
import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.enums.AuthType;
import com.epam.ta.reportportal.entity.integration.Integration;
import com.epam.ta.reportportal.entity.integration.IntegrationParams;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.AllowedValue;
import com.epam.ta.reportportal.ws.model.externalsystem.PostFormField;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import org.jasypt.util.text.BasicTextEncryptor;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static com.epam.ta.reportportal.commons.Predicates.*;
import static com.epam.ta.reportportal.commons.validation.BusinessRule.expect;
import static com.epam.ta.reportportal.commons.validation.Suppliers.formattedSupplier;
import static com.epam.ta.reportportal.ws.model.ErrorType.UNABLE_INTERACT_WITH_INTEGRATION;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

/**
 * JIRA related implementation of {@link BtsExtension}.
 *
 * @author Andrei Varabyeu
 * @author Andrei_Ramanchuk
 */

// TODO REVIEW this class after full JIRA support implementation
@Extension
@Component
public class JiraStrategy implements ReportPortalExtensionPoint, BtsExtension {

	private static final String BUG = "Bug";
	private static final Logger LOGGER = LoggerFactory.getLogger(JiraStrategy.class);

	@Autowired
	@Qualifier("attachmentDataStoreService")
	private DataStoreService dataStoreService;

	@Autowired
	private BasicTextEncryptor simpleEncryptor;

	@Autowired
	private LogRepository logRepository;

	@Autowired
	private TestItemRepository itemRepository;

	private Supplier<JIRATicketDescriptionService> descriptionService = Suppliers.memoize(() -> new JIRATicketDescriptionService(
			logRepository,
			itemRepository
	));

	@Override
	public Map<String, ?> getPluginParams() {
		return Collections.emptyMap();
	}

	@Override
	public PluginCommand getCommandToExecute(String commandName) {
		return null;
	}

	@Override
	public IntegrationGroupEnum getIntegrationGroup() {
		return IntegrationGroupEnum.BTS;
	}

	@Override
	public boolean testConnection(Integration system) {
		IntegrationParams params = ofNullable(system.getParams()).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Integration params are not specified."
		));

		String url = JiraProps.URL.getParam(params)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
		String username = JiraProps.USER_NAME.getParam(params)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Username is not specified."));
		String password = JiraProps.PASSWORD.getParam(params)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Password is not specified."));
		String project = JiraProps.PROJECT.getParam(params)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Project is not specified."));

		validateExternalSystemDetails(system);
		try (JiraRestClient restClient = getClient(url, username, simpleEncryptor.decrypt(password))) {
			return restClient.getProjectClient().getProject(project).claim() != null;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	//    @Cacheable(value = CacheConfiguration.EXTERNAL_SYSTEM_TICKET_CACHE, key = "#system.url + #system.project + #id")
	public Optional<Ticket> getTicket(final String id, Integration system) {
		try (JiraRestClient client = getClient(system.getParams())) {
			return getTicket(id, system.getParams(), client);
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return Optional.empty();
		}
	}

	private Optional<Ticket> getTicket(String id, IntegrationParams details, JiraRestClient jiraRestClient) {
		SearchResult issues = findIssue(id, jiraRestClient);
		if (issues.getTotal() > 0) {
			Issue issue = jiraRestClient.getIssueClient().getIssue(id).claim();
			return Optional.of(JIRATicketUtils.toTicket(issue,
					JiraProps.URL.getParam(details)
							.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
									"Url is not specified."
							))
			));
		} else {
			return Optional.empty();
		}
	}

	@Override
	public Ticket submitTicket(final PostTicketRQ ticketRQ, Integration details) {
		expect(ticketRQ.getFields(), not(isNull())).verify(UNABLE_INTERACT_WITH_INTEGRATION, "External System fields set is empty!");
		List<PostFormField> fields = ticketRQ.getFields();

		// TODO add validation of any field with allowedValues() array
		// Additional validation required for unsupported
		// ticket type and/or components in JIRA.
		PostFormField issueType = new PostFormField();
		PostFormField components = new PostFormField();
		for (PostFormField object : fields) {
			if ("issuetype".equalsIgnoreCase(object.getId())) {
				issueType = object;
			}
			if ("components".equalsIgnoreCase(object.getId())) {
				components = object;
			}
		}

		expect(issueType.getValue().size(), equalTo(1)).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				formattedSupplier("[IssueType] field has multiple values '{}' but should be only one", issueType.getValue())
		);
		final String issueTypeStr = issueType.getValue().get(0);

		try (JiraRestClient client = getClient(details.getParams(), ticketRQ)) {
			Project jiraProject = getProject(client, details);

			if (null != components.getValue()) {
				Set<String> validComponents = StreamSupport.stream(jiraProject.getComponents().spliterator(), false)
						.map(JiraPredicates.COMPONENT_NAMES)
						.collect(toSet());
				validComponents.forEach(component -> expect(component, in(validComponents)).verify(UNABLE_INTERACT_WITH_INTEGRATION,
						formattedSupplier("Component '{}' not exists in the external system", component)
				));
			}

			// TODO consider to modify code below - project cached
			Optional<IssueType> issueTypeOptional = StreamSupport.stream(jiraProject.getIssueTypes().spliterator(), false)
					.filter(input -> issueTypeStr.equalsIgnoreCase(input.getName()))
					.findFirst();

			expect(issueTypeOptional, Preconditions.IS_PRESENT).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					formattedSupplier("Unable post issue with type '{}' for project '{}'.",
							issueType.getValue().get(0),
							details.getProject()
					)
			);
			IssueInput issueInput = JIRATicketUtils.toIssueInput(client,
					jiraProject,
					issueTypeOptional,
					ticketRQ,
					descriptionService.get()
			);

			Map<String, String> binaryData = findBinaryData(issueInput);

			/*
			 * Claim because we wanna be sure everything is OK
			 */
			BasicIssue createdIssue = client.getIssueClient().createIssue(issueInput).claim();

			// post binary data
			Issue issue = client.getIssueClient().getIssue(createdIssue.getKey()).claim();

			AttachmentInput[] attachmentInputs = new AttachmentInput[binaryData.size()];
			int counter = 0;
			for (Map.Entry<String, String> binaryDataEntry : binaryData.entrySet()) {

				Optional<InputStream> data = dataStoreService.load(binaryDataEntry.getKey());
				if (data.isPresent()) {
					attachmentInputs[counter] = new AttachmentInput(binaryDataEntry.getValue(), data.get());
					counter++;
				}
			}
			if (counter != 0) {
				client.getIssueClient().addAttachments(issue.getAttachmentsUri(), Arrays.copyOf(attachmentInputs, counter)).claim();
			}
			return getTicket(createdIssue.getKey(), details.getParams(), client).orElse(null);

		} catch (ReportPortalException e) {
			throw e;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, e.getMessage());
		}
	}

	/**
	 * Get jira's {@link Project} object.
	 *
	 * @param jiraRestClient Jira API client
	 * @param system         Integration system
	 * @return Jira Project
	 */
	// paced in separate method because result object should be cached
	// TODO consider avoiding this method
	//    @Cacheable(value = CacheConfiguration.JIRA_PROJECT_CACHE, key = "#details")
	private Project getProject(JiraRestClient jiraRestClient, Integration system) {
		IntegrationParams params = ofNullable(system.getParams()).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Integration params are not specified."
		));
		return jiraRestClient.getProjectClient()
				.getProject(JiraProps.PROJECT.getParam(params)
						.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
								"Project is not specified."
						)))
				.claim();
	}

	private SearchResult findIssue(String id, JiraRestClient jiraRestClient) {
		return jiraRestClient.getSearchClient().searchJql("issue = " + id).claim();
	}

	/**
	 * Parse ticket description and find binary data
	 *
	 * @param issueInput Jira issue
	 * @return Parsed parameters
	 */
	private Map<String, String> findBinaryData(IssueInput issueInput) {
		Map<String, String> binary = new HashMap<>();
		String description = issueInput.getField(IssueFieldId.DESCRIPTION_FIELD.id).getValue().toString();
		if (null != description) {
			// !54086a2c3c0c7d4446beb3e6.jpg| or [^54086a2c3c0c7d4446beb3e6.xml]
			String regex = "(!|\\[\\^)\\w+\\.\\w{0,10}(\\||\\])";
			Matcher matcher = Pattern.compile(regex).matcher(description);
			while (matcher.find()) {
				String rawValue = description.subSequence(matcher.start(), matcher.end()).toString();
				String binaryDataName = rawValue.replace("!", "").replace("[", "").replace("]", "").replace("^", "").replace("|", "");
				String binaryDataId = binaryDataName.split("\\.")[0];
				binary.put(binaryDataId, binaryDataName);
			}
		}
		return binary;
	}

	@Override
	public List<PostFormField> getTicketFields(final String ticketType, Integration details) {
		List<PostFormField> result = new ArrayList<>();
		try (JiraRestClient client = getClient(details.getParams())) {
			Project jiraProject = getProject(client, details);
			Optional<IssueType> issueType = StreamSupport.stream(jiraProject.getIssueTypes().spliterator(), false)
					.filter(input -> ticketType.equalsIgnoreCase(input.getName()))
					.findFirst();

			BusinessRule.expect(issueType, Preconditions.IS_PRESENT)
					.verify(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Ticket type '" + ticketType + "' not found");

			GetCreateIssueMetadataOptions options = new GetCreateIssueMetadataOptionsBuilder().withExpandedIssueTypesFields()
					.withProjectKeys(jiraProject.getKey())
					.build();
			Iterable<CimProject> projects = client.getIssueClient().getCreateIssueMetadata(options).claim();
			CimProject project = projects.iterator().next();
			CimIssueType cimIssueType = EntityHelper.findEntityById(project.getIssueTypes(), issueType.get().getId());
			for (String key : cimIssueType.getFields().keySet()) {
				List<String> defValue = null;
				CimFieldInfo issueField = cimIssueType.getFields().get(key);
				// Field ID for next JIRA POST ticket requests
				String fieldID = issueField.getId();
				String fieldType = issueField.getSchema().getType();
				List<AllowedValue> allowed = new ArrayList<>();

				// Provide values for custom fields with predefined options
				if (issueField.getAllowedValues() != null) {
					for (Object o : issueField.getAllowedValues()) {
						if (o instanceof CustomFieldOption) {
							CustomFieldOption customField = (CustomFieldOption) o;
							allowed.add(new AllowedValue(String.valueOf(customField.getId()), (customField).getValue()));
						}
					}
				}

				// Field NAME for user friendly UI output (for UI only)
				String fieldName = issueField.getName();

				if (fieldID.equalsIgnoreCase(IssueFieldId.COMPONENTS_FIELD.id)) {
					for (BasicComponent component : jiraProject.getComponents()) {
						allowed.add(new AllowedValue(String.valueOf(component.getId()), component.getName()));
					}
				}
				if (fieldID.equalsIgnoreCase(IssueFieldId.FIX_VERSIONS_FIELD.id)) {
					for (Version version : jiraProject.getVersions()) {
						allowed.add(new AllowedValue(String.valueOf(version.getId()), version.getName()));
					}
				}
				if (fieldID.equalsIgnoreCase(IssueFieldId.AFFECTS_VERSIONS_FIELD.id)) {
					for (Version version : jiraProject.getVersions()) {
						allowed.add(new AllowedValue(String.valueOf(version.getId()), version.getName()));
					}
				}
				if (fieldID.equalsIgnoreCase(IssueFieldId.PRIORITY_FIELD.id)) {
					if (null != cimIssueType.getField(IssueFieldId.PRIORITY_FIELD)) {
						Iterable<Object> allowedValuesForPriority = cimIssueType.getField(IssueFieldId.PRIORITY_FIELD).getAllowedValues();
						for (Object singlePriority : allowedValuesForPriority) {
							BasicPriority priority = (BasicPriority) singlePriority;
							allowed.add(new AllowedValue(String.valueOf(priority.getId()), priority.getName()));
						}
					}
				}
				if (fieldID.equalsIgnoreCase(IssueFieldId.ISSUE_TYPE_FIELD.id)) {
					defValue = Collections.singletonList(ticketType);
				}
				if (fieldID.equalsIgnoreCase(IssueFieldId.ASSIGNEE_FIELD.id)) {
					allowed = getJiraProjectAssignee(jiraProject);
				}

				//@formatter:off
                // Skip project field as external from list
                // Skip attachment cause we are not providing this functionality now
                // Skip timetracking field cause complexity. There are two fields with Original Estimation and Remaining Estimation.
                // Skip Story Link as greenhopper plugin field.
                // Skip Sprint field as complex one.
                //@formatter:on
				if ("project".equalsIgnoreCase(fieldID) || "attachment".equalsIgnoreCase(fieldID)
						|| "timetracking".equalsIgnoreCase(fieldID) || "Epic Link".equalsIgnoreCase(fieldName) || "Sprint".equalsIgnoreCase(
						fieldName)) {
					continue;
				}

				result.add(new PostFormField(fieldID, fieldName, fieldType, issueField.isRequired(), defValue, allowed));
			}
			return result;
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			return new ArrayList<>();
		}

	}

	@Override
	public List<String> getIssueTypes(Integration system) {
		try (JiraRestClient client = getClient(system.getParams())) {
			Project jiraProject = getProject(client, system);
			return StreamSupport.stream(jiraProject.getIssueTypes().spliterator(), false)
					.map(IssueType::getName)
					.collect(Collectors.toList());
		} catch (Exception e) {
			throw new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Check connection settings.");
		}
	}

	/**
	 * JIRA properties validator
	 *
	 * @param details External system details
	 */
	private void validateExternalSystemDetails(Integration details) {
		AuthType authType = AuthType.findByName(JiraProps.AUTH_TYPE.getParam(details.getParams())
				.orElseThrow(() -> new ReportPortalException(UNABLE_INTERACT_WITH_INTEGRATION, "Auth type value cannot be NULL")))
				.orElseThrow(() -> new ReportPortalException(ErrorType.INCORRECT_AUTHENTICATION_TYPE));
		if (AuthType.BASIC.equals(authType)) {
			expect(JiraProps.USER_NAME.getParam(details.getParams()), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					"Username value cannot be NULL"
			);
			expect(JiraProps.PASSWORD.getParam(details.getParams()), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					"Password value cannot be NULL"
			);
		} else if (AuthType.OAUTH.equals(authType)) {
			expect(JiraProps.OAUTH_ACCESS_KEY.getParam(details.getParams()), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
					"AccessKey value cannot be NULL"
			);
		}
		expect(JiraProps.PROJECT.getParam(details.getParams()), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				"JIRA project value cannot be NULL"
		);
		expect(JiraProps.URL.getParam(details.getParams()), isPresent()).verify(UNABLE_INTERACT_WITH_INTEGRATION,
				"JIRA URL value cannot be NULL"
		);
	}

	/**
	 * Get list of project users available for assignee field
	 *
	 * @param jiraProject Project from JIRA
	 * @return List of allowed values
	 */
	private List<AllowedValue> getJiraProjectAssignee(Project jiraProject) {
		Iterable<BasicProjectRole> jiraProjectRoles = jiraProject.getProjectRoles();
		try {
			return StreamSupport.stream(jiraProjectRoles.spliterator(), false)
					.filter(role -> role instanceof ProjectRole)
					.map(role -> (ProjectRole) role)
					.flatMap(role -> StreamSupport.stream(role.getActors().spliterator(), false))
					.distinct()
					.map(actor -> new AllowedValue(String.valueOf(actor.getId()), actor.getDisplayName()))
					.collect(Collectors.toList());
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			throw new ReportPortalException("There is a problem while getting issue types", e);
		}

	}

	public JiraRestClient getClient(String uri, String providedUsername, String providePassword) {
		return new AsynchronousJiraRestClientFactory().create(URI.create(uri),
				new BasicHttpAuthenticationHandler(providedUsername, providePassword)
		);
	}

	public JiraRestClient getClient(IntegrationParams params) {
		String url = JiraProps.URL.getParam(params)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
		String username = JiraProps.USER_NAME.getParam(params)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Username is not specified."));
		String password = JiraProps.PASSWORD.getParam(params)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Password is not specified."));

		return new AsynchronousJiraRestClientFactory().create(URI.create(url),
				new BasicHttpAuthenticationHandler(username, simpleEncryptor.decrypt(password))
		);
	}

	public JiraRestClient getClient(IntegrationParams params, PostTicketRQ postTicketRQ) {
		String url = JiraProps.URL.getParam(params)
				.orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION, "Url is not specified."));
		String username = ofNullable(postTicketRQ.getUsername()).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Username is not specified."
		));
		String password = ofNullable(postTicketRQ.getPassword()).orElseThrow(() -> new ReportPortalException(ErrorType.UNABLE_INTERACT_WITH_INTEGRATION,
				"Password is not specified."
		));

		return new AsynchronousJiraRestClientFactory().create(URI.create(url), new BasicHttpAuthenticationHandler(username, password));
	}

}
