package com.epam.reportportal.extension.bugtracking.jira;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.jasypt.util.text.BasicTextEncryptor;
import org.springframework.context.annotation.Bean;


public class TestConf {

  @Bean
  BasicTextEncryptor basicTextEncryptor() {
    BasicTextEncryptor basicTextEncryptor = new BasicTextEncryptor();
    basicTextEncryptor.setPassword("123");
    return basicTextEncryptor;
  }

  @Bean
  public ObjectMapper objectMapper() {
    ObjectMapper om = new ObjectMapper();
    om.setAnnotationIntrospector(new JacksonAnnotationIntrospector());
    om.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, true);
    om.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    om.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    om.registerModule(new JavaTimeModule());
    return om;
  }
}
