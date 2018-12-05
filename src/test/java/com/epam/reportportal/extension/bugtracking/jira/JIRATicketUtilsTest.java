package com.epam.reportportal.extension.bugtracking.jira;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class JIRATicketUtilsTest {

	@Test
	public void stripEnd() {
		Assert.assertThat("Incorrect truncation", JIRATicketUtils.stripEnd("hello world", "world"), CoreMatchers.equalTo("hello "));
	}
}
