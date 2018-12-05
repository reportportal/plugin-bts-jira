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

import com.epam.ta.reportportal.dao.LogRepository;
import com.epam.ta.reportportal.dao.TestItemRepository;
import com.epam.ta.reportportal.entity.item.TestItem;
import com.epam.ta.reportportal.entity.log.Log;
import com.epam.ta.reportportal.exception.ReportPortalException;
import com.epam.ta.reportportal.ws.model.ErrorType;
import com.epam.ta.reportportal.ws.model.externalsystem.PostTicketRQ;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.mime.MimeType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.mime.MimeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Provide functionality for building jira's ticket description
 *
 * @author Aliaksei_Makayed
 * @author Dzmitry_Kavalets
 */
public class JIRATicketDescriptionService {
	private static final Logger LOGGER = LoggerFactory.getLogger(JIRATicketDescriptionService.class);

	public static final String JIRA_MARKUP_LINE_BREAK = "\\\\ ";
	public static final String BACK_LINK_HEADER = "h3.*Back link to Report Portal:*";
	public static final String BACK_LINK_PATTERN = "[Link to defect|%s]%n";
	public static final String COMMENTS_HEADER = "h3.*Test Item comments:*";
	public static final String CODE = "{code}";
	private static final String IMAGE_CONTENT = "image";
	private static final String IMAGE_HEIGHT_TEMPLATE = "|height=366!";

	private final LogRepository logRepository;
	private final TestItemRepository itemRepository;
	private final DateFormat dateFormat;

	public JIRATicketDescriptionService(LogRepository logRepository, TestItemRepository itemRepository) {
		this.dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
		this.logRepository = logRepository;
		this.itemRepository = itemRepository;
	}

	/**
	 * Generate ticket description using logs of specified test item.
	 *
	 * @param itemIds
	 * @param ticketRQ
	 * @return
	 */
	public String getDescription(Iterable<Long> itemIds, PostTicketRQ ticketRQ) {
		if (IterableUtils.isEmpty(itemIds)) {
			return "";
		}

		TikaConfig tikaConfig = TikaConfig.getDefaultConfig();
		MimeTypes mimeRepository = tikaConfig.getMimeRepository();
		StringBuilder descriptionBuilder = new StringBuilder();

		TestItem item = itemRepository.findById(ticketRQ.getTestItemId())
				.orElseThrow(() -> new ReportPortalException(ErrorType.TEST_ITEM_NOT_FOUND, ticketRQ.getTestItemId()));

		for (Long itemId : itemIds) {
			List<Log> logs = logRepository.findByTestItemId(itemId, ticketRQ.getNumberOfLogs());
			if (StringUtils.isNotBlank(ticketRQ.getBackLinks().get(itemId))) {
				descriptionBuilder.append(BACK_LINK_HEADER);
				descriptionBuilder.append("\n");
				descriptionBuilder.append(" - ");
				descriptionBuilder.append(String.format(BACK_LINK_PATTERN, ticketRQ.getBackLinks().get(itemId)));
				descriptionBuilder.append("\n");
			}
			// For single test-item only
			// TODO add multiple test-items backlinks
			if (ticketRQ.getIsIncludeComments() && (ticketRQ.getBackLinks().size() == 1)) {
				if (StringUtils.isNotBlank(ticketRQ.getBackLinks().get(itemId))) {

					// If test-item contains any comments, then add it for JIRA
					// comments section
					//TODO a lot of possible NPEs
					if (StringUtils.isNotBlank(item.getItemResults().getIssue().getIssueDescription())) {
						descriptionBuilder.append(COMMENTS_HEADER);
						descriptionBuilder.append("\n");
						descriptionBuilder.append(item.getItemResults().getIssue().getIssueDescription());
						descriptionBuilder.append("\n");
					}
				}
			}
			if (CollectionUtils.isNotEmpty(logs) && (ticketRQ.getIsIncludeLogs() || ticketRQ.getIsIncludeScreenshots())) {
				descriptionBuilder.append("h3.*Test execution log:*\n");
				descriptionBuilder.append(
						"{panel:title=Test execution log|borderStyle=solid|borderColor=#ccc|titleColor=#34302D|titleBGColor=#6DB33F}");
				for (Log log : logs) {
					if (ticketRQ.getIsIncludeLogs()) {
						descriptionBuilder.append(CODE).append(getFormattedMessage(log)).append(CODE);
					}
					if (StringUtils.isNotBlank(log.getContentType()) && StringUtils.isNotBlank(log.getAttachment())
							&& ticketRQ.getIsIncludeScreenshots()) {
						try {
							MimeType mimeType = mimeRepository.forName(log.getContentType());
							if (log.getContentType().contains(IMAGE_CONTENT)) {
								descriptionBuilder.append("!")
										.append(log.getAttachment())
										.append(mimeType.getExtension())
										.append(IMAGE_HEIGHT_TEMPLATE);
							} else {
								descriptionBuilder.append("[^").append(log.getAttachment()).append(mimeType.getExtension()).append("]");
							}
							descriptionBuilder.append(JIRA_MARKUP_LINE_BREAK);
						} catch (MimeTypeException e) {
							descriptionBuilder.append(JIRA_MARKUP_LINE_BREAK);
							LOGGER.error("JIRATicketDescriptionService error: " + e.getMessage(), e);
						}

					}
				}
				descriptionBuilder.append("{panel}\n");
			}
		}
		return descriptionBuilder.toString();
	}

	private String getFormattedMessage(Log log) {
		StringBuilder messageBuilder = new StringBuilder();
		if (log.getLogTime() != null) {
			messageBuilder.append(" Time: ").append(dateFormat.format(log.getLogTime())).append(", ");
		}
		if (log.getLogLevel() != null) {
			messageBuilder.append("Level: ").append(log.getLogLevel()).append(", ");
		}
		messageBuilder.append("Log: ").append(log.getLogMessage()).append("\n");
		return messageBuilder.toString();
	}
}