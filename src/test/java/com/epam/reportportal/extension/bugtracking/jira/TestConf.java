package com.epam.reportportal.extension.bugtracking.jira;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.context.annotation.Bean;
import tools.jackson.databind.MapperFeature;
import tools.jackson.databind.json.JsonMapper;


public class TestConf {

  @Bean
  BasicTextEncryptor basicTextEncryptor() {
    BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
    basicTextEncryptor.setPassword("123");
    return basicTextEncryptor;
  }

  @Bean
  public JsonMapper objectMapper() {
    return testJsonMapper();
  }

  /**
   * Mirrors the host mapper configuration of service-api ({@code JacksonConfiguration}).
   */
  public static JsonMapper testJsonMapper() {
    return JsonMapper.builder()
        .disable(MapperFeature.DETECT_PARAMETER_NAMES)
        .enable(MapperFeature.DEFAULT_VIEW_INCLUSION)
        .changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(Include.NON_NULL))
        .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
        .build();
  }
}
