package com.epam.reportportal.extension.bugtracking.jira;


import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;

public class JIRATicketUtilsTest {

  @Test
  public void stripEnd() {
    MatcherAssert.assertThat("Incorrect truncation", JIRATicketUtils.stripEnd("hello world", "world"), CoreMatchers.equalTo("hello "));
  }
}
