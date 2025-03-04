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

public class SampleData {

  public static final String EPIC = """
            {
        "includeComments": true,
        "includeData": true,
        "includeLogs": true,
        "logQuantity": 50,
        "item": 4713343083,
        "fields": [
          {
            "id": "issuetype",
            "value": [
              "Epic"
            ],
            "required": true,
            "fieldName": "Issue Type",
            "fieldType": "issuetype",
            "definedValues": []
          },
          {
            "id": "summary",
            "required": true,
            "fieldName": "Summary",
            "fieldType": "string",
            "definedValues": [],
            "value": [
              "Test by Oleg"
            ]
          }
        ],
        "backLinks": {
          "4713343083": "https://reportportal.epam.com/ui/#epm-rpp/launches/all/8636133/4713341992/4713343083/log?item0Params=filter.eq.hasStats%3Dtrue%26filter.eq.hasChildren%3Dfalse%26filter.in.issueType%3Dti001%252Cti_qdpzzqt1yulh%252Cti_qhqltb6q9y5y%252Cti_vb94w7l7xlpu%252Cti_skk1lqxyxfsi"
        }
      }
      """;

  public static String STORY = """
        {
        "includeComments": true,
        "includeData": true,
        "includeLogs": true,
        "logQuantity": 50,
        "item": 310776,
        "fields": [
          {
            "fieldName": "Summary",
            "id": "summary",
            "fieldType": "string",
            "required": true,
            "definedValues": [],
            "value": [
              "test"
            ]
          },
          {
            "fieldName": "Issue Type",
            "id": "issuetype",
            "fieldType": "issuetype",
            "required": true,
            "value": [
              "Story"
            ],
            "definedValues": [],
            "disabled": true
          },
          {
            "fieldName": "Labels",
            "id": "labels",
            "fieldType": "array",
            "required": false,
            "definedValues": [],
            "value": [
              "55"
            ]
          }
        ],
        "backLinks": {
          "310776": "http://dev.epmrpp.reportportal.io/ui/#olya/launches/all/82481/310771/310772/310776/log?item0Params=filter.eq.hasStats%3Dtrue%26filter.eq.hasChildren%3Dfalse%26filter.in.issueType%3Dpb001"
        }
      }""";

}
