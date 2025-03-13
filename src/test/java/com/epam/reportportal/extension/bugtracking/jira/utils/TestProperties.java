/*
 * Copyright 2025 EPAM Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.epam.reportportal.extension.bugtracking.jira.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import lombok.SneakyThrows;

public class TestProperties {

  private TestProperties() {
  }

  @SneakyThrows
  public static Properties getTestProperties(String path) {
    Properties properties = new Properties();
    try (var inputStream = TestProperties.class.getClassLoader()
        .getResourceAsStream(path)) {
      if (inputStream == null) {
        throw new FileNotFoundException(path);
      }
      properties.load(inputStream);
    } catch (IOException e) {
      throw new RuntimeException(
          "Could not read resource file: " + e);
    }
    return properties;
  }

}
