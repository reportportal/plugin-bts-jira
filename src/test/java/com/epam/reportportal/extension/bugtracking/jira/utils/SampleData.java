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

  public static String DEFECT = """
             {
                 "includeComments": true,
                 "includeData": true,
                 "includeLogs": true,
                 "logQuantity": 50,
                 "item": 342243,
                 "fields": [
                     {
                         "id": "issuetype",
                         "value": [
                             "Defect"
                         ],
                         "required": true,
                         "fieldName": "Issue Type",
                         "fieldType": "issuetype",
                         "definedValues": [],
                         "disabled": true
                     },
                     {
                         "id": "summary",
                         "required": true,
                         "fieldName": "Summary",
                         "fieldType": "string",
                         "definedValues": [],
                         "value": [
                             "Test"
                         ]
                     },
                       {
                                 "id": "priority",
                                 "value": [
                                     "Trivial"
                                 ],
                                 "required": false,
                                 "fieldName": "Priority",
                                 "fieldType": "priority",
                                 "definedValues": [
                                     {
                                         "valueId": "1",
                                         "valueName": "Blocker"
                                     },
                                     {
                                         "valueId": "2",
                                         "valueName": "Critical"
                                     },
                                     {
                                         "valueId": "3",
                                         "valueName": "Major"
                                     },
                                     {
                                         "valueId": "4",
                                         "valueName": "Minor"
                                     },
                                     {
                                         "valueId": "5",
                                         "valueName": "Trivial"
                                     },
                                     {
                                         "valueId": "10700",
                                         "valueName": "None"
                                     }
                                 ]
                             },
                     {
                         "id": "customfield_11000",
                         "value": [
                             "Sev-0"
                         ],
                         "required": false,
                         "fieldName": "Severity",
                         "fieldType": "option",
                         "definedValues": [
                             {
                                 "valueId": "None",
                                 "valueName": "None"
                             },
                             {
                                 "valueId": "52973",
                                 "valueName": "Sev-0"
                             },
                             {
                                 "valueId": "52974",
                                 "valueName": "Sev-1"
                             },
                             {
                                 "valueId": "52975",
                                 "valueName": "Sev-2"
                             },
                             {
                                 "valueId": "52976",
                                 "valueName": "Sev-3"
                             }
                         ]
                     },
                     {
                         "id": "priority",
                         "value": [
                             "Priority"
                         ],
                         "required": false,
                         "fieldName": "Priority",
                         "fieldType": "priority",
                         "definedValues": [
                             {
                                 "valueId": "None",
                                 "valueName": "None"
                             },
                             {
                                 "valueId": "priority",
                                 "valueName": "Priority"
                             }
                         ]
                     },
                     {
                         "id": "components",
                         "required": false,
                         "fieldName": "Component/s",
                         "fieldType": "array",
                         "definedValues": [
                             {
                                 "valueId": "52109",
                                 "valueName": "Administrate area (general)"
                             },
                             {
                                 "valueId": "52410",
                                 "valueName": "Agents Integration"
                             },
                             {
                                 "valueId": "62910",
                                 "valueName": "Aggregator"
                             },
                             {
                                 "valueId": "87745",
                                 "valueName": "Analysis"
                             },
                             {
                                 "valueId": "71876",
                                 "valueName": "Analyzer"
                             },
                             {
                                 "valueId": "85757",
                                 "valueName": "API"
                             },
                             {
                                 "valueId": "55228",
                                 "valueName": "API (Swagger)"
                             },
                             {
                                 "valueId": "71408",
                                 "valueName": "Attachments"
                             },
                             {
                                 "valueId": "71320",
                                 "valueName": "Attributes"
                             },
                             {
                                 "valueId": "71659",
                                 "valueName": "Authorization plugins and integrations"
                             },
                             {
                                 "valueId": "74846",
                                 "valueName": "Billing"
                             },
                             {
                                 "valueId": "52119",
                                 "valueName": "BTS plugins and integrations"
                             },
                             {
                                 "valueId": "52108",
                                 "valueName": "Dashboards"
                             },
                             {
                                 "valueId": "52412",
                                 "valueName": "Debug"
                             },
                             {
                                 "valueId": "86811",
                                 "valueName": "Defect Types"
                             },
                             {
                                 "valueId": "72308",
                                 "valueName": "Demo data"
                             },
                             {
                                 "valueId": "94609",
                                 "valueName": "Docs_CU-24.2.1"
                             },
                             {
                                 "valueId": "97346",
                                 "valueName": "Docs_CU-24.2.2"
                             },
                             {
                                 "valueId": "85246",
                                 "valueName": "Documentation"
                             },
                             {
                                 "valueId": "52409",
                                 "valueName": "E-mail integration"
                             },
                             {
                                 "valueId": "86972",
                                 "valueName": "E-mail templates"
                             },
                             {
                                 "valueId": "58123",
                                 "valueName": "EPAM specific"
                             },
                             {
                                 "valueId": "71893",
                                 "valueName": "Events monitoring"
                             },
                             {
                                 "valueId": "52115",
                                 "valueName": "Filters"
                             },
                             {
                                 "valueId": "78613",
                                 "valueName": "Google Analytics"
                             },
                             {
                                 "valueId": "70704",
                                 "valueName": "History line"
                             },
                             {
                                 "valueId": "70703",
                                 "valueName": "History table"
                             },
                             {
                                 "valueId": "86744",
                                 "valueName": "Import"
                             },
                             {
                                 "valueId": "70610",
                                 "valueName": "Items"
                             },
                             {
                                 "valueId": "78609",
                                 "valueName": "Jobs"
                             },
                             {
                                 "valueId": "53215",
                                 "valueName": "Landing Page"
                             },
                             {
                                 "valueId": "52116",
                                 "valueName": "Launches"
                             },
                             {
                                 "valueId": "89193",
                                 "valueName": "LLM"
                             },
                             {
                                 "valueId": "73827",
                                 "valueName": "Localization"
                             },
                             {
                                 "valueId": "52408",
                                 "valueName": "Login page"
                             },
                             {
                                 "valueId": "52117",
                                 "valueName": "Logs"
                             },
                             {
                                 "valueId": "52118",
                                 "valueName": "Make decision modal"
                             },
                             {
                                 "valueId": "72657",
                                 "valueName": "Mobile view"
                             },
                             {
                                 "valueId": "72791",
                                 "valueName": "Nested Steps"
                             },
                             {
                                 "valueId": "86701",
                                 "valueName": "NFR"
                             },
                             {
                                 "valueId": "92096",
                                 "valueName": "Organization info"
                             },
                             {
                                 "valueId": "81227",
                                 "valueName": "Organization level"
                             },
                             {
                                 "valueId": "92097",
                                 "valueName": "Organization settings"
                             },
                             {
                                 "valueId": "92094",
                                 "valueName": "Organization users"
                             },
                             {
                                 "valueId": "92098",
                                 "valueName": "Organizations management"
                             },
                             {
                                 "valueId": "86810",
                                 "valueName": "Pattern-Analysis"
                             },
                             {
                                 "valueId": "85756",
                                 "valueName": "Performance"
                             },
                             {
                                 "valueId": "73805",
                                 "valueName": "Plugins and integrations (general)"
                             },
                             {
                                 "valueId": "81839",
                                 "valueName": "Portfolio dashboard"
                             },
                             {
                                 "valueId": "52114",
                                 "valueName": "Profile"
                             },
                             {
                                 "valueId": "73825",
                                 "valueName": "Project members"
                             },
                             {
                                 "valueId": "52112",
                                 "valueName": "Project Settings"
                             },
                             {
                                 "valueId": "57415",
                                 "valueName": "Project's detailed info"
                             },
                             {
                                 "valueId": "52110",
                                 "valueName": "Projects management"
                             },
                             {
                                 "valueId": "85789",
                                 "valueName": "QA"
                             },
                             {
                                 "valueId": "78608",
                                 "valueName": "Quality Gates"
                             },
                             {
                                 "valueId": "73826",
                                 "valueName": "Reporting (UI)"
                             },
                             {
                                 "valueId": "73804",
                                 "valueName": "Retries"
                             },
                             {
                                 "valueId": "73828",
                                 "valueName": "SauceLabs"
                             },
                             {
                                 "valueId": "73810",
                                 "valueName": "Server Settings"
                             },
                             {
                                 "valueId": "82502",
                                 "valueName": "Sign Up flow"
                             },
                             {
                                 "valueId": "85755",
                                 "valueName": "Test Case Library "
                             },
                             {
                                 "valueId": "62823",
                                 "valueName": "UI"
                             },
                             {
                                 "valueId": "81074",
                                 "valueName": "Unique errors"
                             },
                             {
                                 "valueId": "52111",
                                 "valueName": "User management"
                             },
                             {
                                 "valueId": "52106",
                                 "valueName": "Widgets"
                             }
                         ],
                         "value": [
                             "Organization users"
                         ]
                     },
                     {
                         "id": "customfield_14817",
                         "value": [
                             "USD"
                         ],
                         "required": false,
                         "fieldName": "Category",
                         "fieldType": "option",
                         "definedValues": [
                             {
                                 "valueId": "None",
                                 "valueName": "None"
                             },
                             {
                                 "valueId": "50832",
                                 "valueName": "Handover - Discovery - Setup"
                             },
                             {
                                 "valueId": "31524",
                                 "valueName": "USD"
                             },
                             {
                                 "valueId": "50833",
                                 "valueName": "Development - Strategy"
                             },
                             {
                                 "valueId": "50834",
                                 "valueName": "Maintenance - Support"
                             }
                         ]
                     },
                     {
                         "id": "versions",
                         "required": false,
                         "fieldName": "Affects Version/s",
                         "fieldType": "array",
                         "definedValues": [
                             {
                                 "valueId": "68014",
                                 "valueName": "R3 v2.2"
                             },
                             {
                                 "valueId": "69373",
                                 "valueName": "TA Report Portal R3"
                             },
                             {
                                 "valueId": "78887",
                                 "valueName": "R3 2.5"
                             },
                             {
                                 "valueId": "80710",
                                 "valueName": "R3 2.1 Beta1"
                             },
                             {
                                 "valueId": "80711",
                                 "valueName": "R3 2.1 Beta2"
                             },
                             {
                                 "valueId": "81819",
                                 "valueName": "R3 2.6"
                             },
                             {
                                 "valueId": "82629",
                                 "valueName": "R3 2.7"
                             },
                             {
                                 "valueId": "82630",
                                 "valueName": "R3 3.0"
                             },
                             {
                                 "valueId": "82813",
                                 "valueName": "R3 3.1"
                             },
                             {
                                 "valueId": "84412",
                                 "valueName": "3.0.2"
                             },
                             {
                                 "valueId": "84451",
                                 "valueName": "3.0.3"
                             },
                             {
                                 "valueId": "86571",
                                 "valueName": "3.1"
                             },
                             {
                                 "valueId": "86584",
                                 "valueName": "3.2"
                             },
                             {
                                 "valueId": "87361",
                                 "valueName": "3.3"
                             },
                             {
                                 "valueId": "87566",
                                 "valueName": "3.4"
                             },
                             {
                                 "valueId": "89279",
                                 "valueName": "4.0"
                             },
                             {
                                 "valueId": "93494",
                                 "valueName": "4.1"
                             },
                             {
                                 "valueId": "93495",
                                 "valueName": "4.1.1"
                             },
                             {
                                 "valueId": "93497",
                                 "valueName": "4.2"
                             },
                             {
                                 "valueId": "100348",
                                 "valueName": "4.3"
                             },
                             {
                                 "valueId": "103316",
                                 "valueName": "4.2.1"
                             },
                             {
                                 "valueId": "108259",
                                 "valueName": "4.2.3"
                             },
                             {
                                 "valueId": "116107",
                                 "valueName": "4.3.6"
                             },
                             {
                                 "valueId": "137906",
                                 "valueName": "4.4"
                             },
                             {
                                 "valueId": "99701",
                                 "valueName": "5.0"
                             },
                             {
                                 "valueId": "144294",
                                 "valueName": "5.0 Final"
                             },
                             {
                                 "valueId": "144648",
                                 "valueName": "5.1"
                             },
                             {
                                 "valueId": "153031",
                                 "valueName": "5.2"
                             },
                             {
                                 "valueId": "188722",
                                 "valueName": "5.2.2"
                             },
                             {
                                 "valueId": "194117",
                                 "valueName": "5.2.3"
                             },
                             {
                                 "valueId": "163419",
                                 "valueName": "5.3"
                             },
                             {
                                 "valueId": "181674",
                                 "valueName": "5.3.1"
                             },
                             {
                                 "valueId": "207642",
                                 "valueName": "5.3.2"
                             },
                             {
                                 "valueId": "210738",
                                 "valueName": "5.3.3"
                             },
                             {
                                 "valueId": "216402",
                                 "valueName": "5.3.4"
                             },
                             {
                                 "valueId": "221085",
                                 "valueName": "5.3.5"
                             },
                             {
                                 "valueId": "226155",
                                 "valueName": "DesignReleaseFebruary"
                             },
                             {
                                 "valueId": "226156",
                                 "valueName": "PerfReleaseFeb2021"
                             },
                             {
                                 "valueId": "226157",
                                 "valueName": "AgentReleaseFeb2021"
                             },
                             {
                                 "valueId": "230265",
                                 "valueName": "Cleanup Release - March2021"
                             },
                             {
                                 "valueId": "163423",
                                 "valueName": "5.4"
                             },
                             {
                                 "valueId": "234383",
                                 "valueName": "AgentReleaseMay2021"
                             },
                             {
                                 "valueId": "234395",
                                 "valueName": "DevOpsReleaseMay2021"
                             },
                             {
                                 "valueId": "234396",
                                 "valueName": "PerfReleaseMay2021"
                             },
                             {
                                 "valueId": "237248",
                                 "valueName": "5.6"
                             },
                             {
                                 "valueId": "219539",
                                 "valueName": "5.5"
                             },
                             {
                                 "valueId": "243522",
                                 "valueName": "5.5.1"
                             },
                             {
                                 "valueId": "251952",
                                 "valueName": "5.6.1"
                             },
                             {
                                 "valueId": "252258",
                                 "valueName": "5.6.2"
                             },
                             {
                                 "valueId": "252729",
                                 "valueName": "AgentReleaseDec2021"
                             },
                             {
                                 "valueId": "252730",
                                 "valueName": "DesignReleaseDec2021"
                             },
                             {
                                 "valueId": "252731",
                                 "valueName": "DevOpsReleaseDec2021"
                             },
                             {
                                 "valueId": "252732",
                                 "valueName": "PerformanceReleaseDec2021"
                             },
                             {
                                 "valueId": "252836",
                                 "valueName": "5.6.3"
                             },
                             {
                                 "valueId": "253799",
                                 "valueName": "5.6.6"
                             },
                             {
                                 "valueId": "254111",
                                 "valueName": "5.6.7"
                             },
                             {
                                 "valueId": "219540",
                                 "valueName": "5.7"
                             },
                             {
                                 "valueId": "254118",
                                 "valueName": "5.7.1"
                             },
                             {
                                 "valueId": "256802",
                                 "valueName": "5.7.2"
                             },
                             {
                                 "valueId": "210724",
                                 "valueName": "RP-23.2"
                             },
                             {
                                 "valueId": "260302",
                                 "valueName": "RP-23.1"
                             },
                             {
                                 "valueId": "259354",
                                 "valueName": "5.7.4"
                             },
                             {
                                 "valueId": "257556",
                                 "valueName": "5.7.3"
                             },
                             {
                                 "valueId": "260723",
                                 "valueName": "billing-dedicated-1.0"
                             },
                             {
                                 "valueId": "260865",
                                 "valueName": "billing-saas-1.0"
                             },
                             {
                                 "valueId": "262356",
                                 "valueName": "DevOpsReleaseJan2023"
                             },
                             {
                                 "valueId": "264926",
                                 "valueName": "Disney Release"
                             },
                             {
                                 "valueId": "265040",
                                 "valueName": "service-ui-5.8.0"
                             },
                             {
                                 "valueId": "265068",
                                 "valueName": "service-api-5.8.0"
                             },
                             {
                                 "valueId": "265470",
                                 "valueName": "migrations-5.9.0"
                             },
                             {
                                 "valueId": "265471",
                                 "valueName": "commons-dao-5.9.0"
                             },
                             {
                                 "valueId": "265472",
                                 "valueName": "commons-model-5.8.1"
                             },
                             {
                                 "valueId": "265516",
                                 "valueName": "service-api-5.9.0"
                             },
                             {
                                 "valueId": "265517",
                                 "valueName": "service-authorization-5.8.1"
                             },
                             {
                                 "valueId": "265524",
                                 "valueName": "service-ui-5.9.0"
                             },
                             {
                                 "valueId": "265525",
                                 "valueName": "service-jobs-5.8.1"
                             },
                             {
                                 "valueId": "265716",
                                 "valueName": "commons-dao-5.9.2"
                             },
                             {
                                 "valueId": "265717",
                                 "valueName": "service-jobs-5.8.2"
                             },
                             {
                                 "valueId": "265718",
                                 "valueName": "service-authorization-5.8.2"
                             },
                             {
                                 "valueId": "265719",
                                 "valueName": "service-api-5.9.1"
                             },
                             {
                                 "valueId": "265836",
                                 "valueName": "service-api-5.9.2"
                             },
                             {
                                 "valueId": "265848",
                                 "valueName": "Bitly"
                             },
                             {
                                 "valueId": "265957",
                                 "valueName": "migrations-5.10.0"
                             },
                             {
                                 "valueId": "265961",
                                 "valueName": "commons-dao-5.10.0"
                             },
                             {
                                 "valueId": "265963",
                                 "valueName": "commons-model-5.10.0"
                             },
                             {
                                 "valueId": "266084",
                                 "valueName": "service-ui-5.10.0"
                             },
                             {
                                 "valueId": "266242",
                                 "valueName": "service-jobs-5.10.0"
                             },
                             {
                                 "valueId": "266253",
                                 "valueName": "service-api-5.10.0"
                             },
                             {
                                 "valueId": "266254",
                                 "valueName": "service-authorization-5.10.0"
                             },
                             {
                                 "valueId": "266255",
                                 "valueName": "service-auto-analyzer-5.10.0"
                             },
                             {
                                 "valueId": "270921",
                                 "valueName": "RP-24.1"
                             },
                             {
                                 "valueId": "271023",
                                 "valueName": "commons-model-5.11.0"
                             },
                             {
                                 "valueId": "271025",
                                 "valueName": "migrations-5.11.0"
                             },
                             {
                                 "valueId": "271026",
                                 "valueName": "commons-dao-5.11.0"
                             },
                             {
                                 "valueId": "271028",
                                 "valueName": "service-api-5.11.0"
                             },
                             {
                                 "valueId": "271029",
                                 "valueName": "commons-model-5.11.1"
                             },
                             {
                                 "valueId": "271031",
                                 "valueName": "commons-dao-5.11.1"
                             },
                             {
                                 "valueId": "271032",
                                 "valueName": "service-authorization-5.11.0"
                             },
                             {
                                 "valueId": "271033",
                                 "valueName": "service-auto-analyzer-5.11.0"
                             },
                             {
                                 "valueId": "271035",
                                 "valueName": "service-ui-5.11.0"
                             },
                             {
                                 "valueId": "271251",
                                 "valueName": "RP-24.2"
                             },
                             {
                                 "valueId": "271563",
                                 "valueName": "AgentRelease2024"
                             },
                             {
                                 "valueId": "271564",
                                 "valueName": "DesignRelease2024"
                             },
                             {
                                 "valueId": "271565",
                                 "valueName": "DevOpsRelease2024"
                             },
                             {
                                 "valueId": "271566",
                                 "valueName": "PerfBoardRelease2024"
                             },
                             {
                                 "valueId": "271570",
                                 "valueName": "commons-dao-5.11.2"
                             },
                             {
                                 "valueId": "274907",
                                 "valueName": "CU-24.1.1"
                             },
                             {
                                 "valueId": "276416",
                                 "valueName": "RP-24.3"
                             },
                             {
                                 "valueId": "276673",
                                 "valueName": "RP-25.1"
                             },
                             {
                                 "valueId": "295207",
                                 "valueName": "migrations-5.12.0"
                             },
                             {
                                 "valueId": "295320",
                                 "valueName": "service-jobs-5.12.0"
                             },
                             {
                                 "valueId": "295331",
                                 "valueName": "service-api-5.12.0"
                             },
                             {
                                 "valueId": "295333",
                                 "valueName": "service-authorization-5.12.0"
                             },
                             {
                                 "valueId": "295340",
                                 "valueName": "service-ui-5.12.0"
                             },
                             {
                                 "valueId": "298358",
                                 "valueName": "CU-24.2.1"
                             },
                             {
                                 "valueId": "310231",
                                 "valueName": "service-api-5.12.1"
                             },
                             {
                                 "valueId": "310389",
                                 "valueName": "CU-24.2.2"
                             },
                             {
                                 "valueId": "310390",
                                 "valueName": "CU-24.2.3"
                             },
                             {
                                 "valueId": "316740",
                                 "valueName": "service-api-5.13.0"
                             },
                             {
                                 "valueId": "316742",
                                 "valueName": "service-authorization-5.13.0"
                             },
                             {
                                 "valueId": "316743",
                                 "valueName": "service-jobs-5.13.0"
                             },
                             {
                                 "valueId": "316745",
                                 "valueName": "service-auto-analyzer-5.13.0"
                             },
                             {
                                 "valueId": "319781",
                                 "valueName": "service-ui-5.12.1"
                             },
                             {
                                 "valueId": "324953",
                                 "valueName": "service-api-5.13.1"
                             },
                             {
                                 "valueId": "324958",
                                 "valueName": "service-metrics-gatherer-5.13.0"
                             },
                             {
                                 "valueId": "327772",
                                 "valueName": "RP-25.2"
                             },
                             {
                                 "valueId": "328143",
                                 "valueName": "API"
                             },
                             {
                                 "valueId": "328298",
                                 "valueName": "service-ui-5.12.2"
                             },
                             {
                                 "valueId": "328742",
                                 "valueName": "service-api-5.13.2"
                             },
                             {
                                 "valueId": "329349",
                                 "valueName": "service-ui-5.12.3"
                             },
                             {
                                 "valueId": "329355",
                                 "valueName": "RP-25.3"
                             },
                             {
                                 "valueId": "332002",
                                 "valueName": "service-jobs-5.13.1"
                             },
                             {
                                 "valueId": "332003",
                                 "valueName": "service-authorization-5.13.1"
                             },
                             {
                                 "valueId": "332004",
                                 "valueName": "service-api-5.13.3"
                             },
                             {
                                 "valueId": "335260",
                                 "valueName": "CU-25.0.4"
                             },
                             {
                                 "valueId": "335274",
                                 "valueName": "service-api-5.13.4"
                             },
                             {
                                 "valueId": "335410",
                                 "valueName": "service-auto-analyzer-5.13.2"
                             },
                             {
                                 "valueId": "336087",
                                 "valueName": "service-ui-5.12.4"
                             },
                             {
                                 "valueId": "336240",
                                 "valueName": "CU-25.0.5"
                             },
                             {
                                 "valueId": "336248",
                                 "valueName": "service-api-5.13.5"
                             }
                         ],
                         "value": [
                             "RP-25.1"
                         ]
                     },
                     {
                         "id": "fixVersions",
                         "required": false,
                         "fieldName": "Fix Version/s",
                         "fieldType": "array",
                         "definedValues": [
                             {
                                 "valueId": "68014",
                                 "valueName": "R3 v2.2"
                             },
                             {
                                 "valueId": "69373",
                                 "valueName": "TA Report Portal R3"
                             },
                             {
                                 "valueId": "78887",
                                 "valueName": "R3 2.5"
                             },
                             {
                                 "valueId": "80710",
                                 "valueName": "R3 2.1 Beta1"
                             },
                             {
                                 "valueId": "80711",
                                 "valueName": "R3 2.1 Beta2"
                             },
                             {
                                 "valueId": "81819",
                                 "valueName": "R3 2.6"
                             },
                             {
                                 "valueId": "82629",
                                 "valueName": "R3 2.7"
                             },
                             {
                                 "valueId": "82630",
                                 "valueName": "R3 3.0"
                             },
                             {
                                 "valueId": "82813",
                                 "valueName": "R3 3.1"
                             },
                             {
                                 "valueId": "84412",
                                 "valueName": "3.0.2"
                             },
                             {
                                 "valueId": "84451",
                                 "valueName": "3.0.3"
                             },
                             {
                                 "valueId": "86571",
                                 "valueName": "3.1"
                             },
                             {
                                 "valueId": "86584",
                                 "valueName": "3.2"
                             },
                             {
                                 "valueId": "87361",
                                 "valueName": "3.3"
                             },
                             {
                                 "valueId": "87566",
                                 "valueName": "3.4"
                             },
                             {
                                 "valueId": "89279",
                                 "valueName": "4.0"
                             },
                             {
                                 "valueId": "93494",
                                 "valueName": "4.1"
                             },
                             {
                                 "valueId": "93495",
                                 "valueName": "4.1.1"
                             },
                             {
                                 "valueId": "93497",
                                 "valueName": "4.2"
                             },
                             {
                                 "valueId": "100348",
                                 "valueName": "4.3"
                             },
                             {
                                 "valueId": "103316",
                                 "valueName": "4.2.1"
                             },
                             {
                                 "valueId": "108259",
                                 "valueName": "4.2.3"
                             },
                             {
                                 "valueId": "116107",
                                 "valueName": "4.3.6"
                             },
                             {
                                 "valueId": "137906",
                                 "valueName": "4.4"
                             },
                             {
                                 "valueId": "99701",
                                 "valueName": "5.0"
                             },
                             {
                                 "valueId": "144294",
                                 "valueName": "5.0 Final"
                             },
                             {
                                 "valueId": "144648",
                                 "valueName": "5.1"
                             },
                             {
                                 "valueId": "153031",
                                 "valueName": "5.2"
                             },
                             {
                                 "valueId": "188722",
                                 "valueName": "5.2.2"
                             },
                             {
                                 "valueId": "194117",
                                 "valueName": "5.2.3"
                             },
                             {
                                 "valueId": "163419",
                                 "valueName": "5.3"
                             },
                             {
                                 "valueId": "181674",
                                 "valueName": "5.3.1"
                             },
                             {
                                 "valueId": "207642",
                                 "valueName": "5.3.2"
                             },
                             {
                                 "valueId": "210738",
                                 "valueName": "5.3.3"
                             },
                             {
                                 "valueId": "216402",
                                 "valueName": "5.3.4"
                             },
                             {
                                 "valueId": "221085",
                                 "valueName": "5.3.5"
                             },
                             {
                                 "valueId": "226155",
                                 "valueName": "DesignReleaseFebruary"
                             },
                             {
                                 "valueId": "226156",
                                 "valueName": "PerfReleaseFeb2021"
                             },
                             {
                                 "valueId": "226157",
                                 "valueName": "AgentReleaseFeb2021"
                             },
                             {
                                 "valueId": "230265",
                                 "valueName": "Cleanup Release - March2021"
                             },
                             {
                                 "valueId": "163423",
                                 "valueName": "5.4"
                             },
                             {
                                 "valueId": "234383",
                                 "valueName": "AgentReleaseMay2021"
                             },
                             {
                                 "valueId": "234395",
                                 "valueName": "DevOpsReleaseMay2021"
                             },
                             {
                                 "valueId": "234396",
                                 "valueName": "PerfReleaseMay2021"
                             },
                             {
                                 "valueId": "237248",
                                 "valueName": "5.6"
                             },
                             {
                                 "valueId": "219539",
                                 "valueName": "5.5"
                             },
                             {
                                 "valueId": "243522",
                                 "valueName": "5.5.1"
                             },
                             {
                                 "valueId": "251952",
                                 "valueName": "5.6.1"
                             },
                             {
                                 "valueId": "252258",
                                 "valueName": "5.6.2"
                             },
                             {
                                 "valueId": "252729",
                                 "valueName": "AgentReleaseDec2021"
                             },
                             {
                                 "valueId": "252730",
                                 "valueName": "DesignReleaseDec2021"
                             },
                             {
                                 "valueId": "252731",
                                 "valueName": "DevOpsReleaseDec2021"
                             },
                             {
                                 "valueId": "252732",
                                 "valueName": "PerformanceReleaseDec2021"
                             },
                             {
                                 "valueId": "252836",
                                 "valueName": "5.6.3"
                             },
                             {
                                 "valueId": "253799",
                                 "valueName": "5.6.6"
                             },
                             {
                                 "valueId": "254111",
                                 "valueName": "5.6.7"
                             },
                             {
                                 "valueId": "219540",
                                 "valueName": "5.7"
                             },
                             {
                                 "valueId": "254118",
                                 "valueName": "5.7.1"
                             },
                             {
                                 "valueId": "256802",
                                 "valueName": "5.7.2"
                             },
                             {
                                 "valueId": "210724",
                                 "valueName": "RP-23.2"
                             },
                             {
                                 "valueId": "260302",
                                 "valueName": "RP-23.1"
                             },
                             {
                                 "valueId": "259354",
                                 "valueName": "5.7.4"
                             },
                             {
                                 "valueId": "257556",
                                 "valueName": "5.7.3"
                             },
                             {
                                 "valueId": "260723",
                                 "valueName": "billing-dedicated-1.0"
                             },
                             {
                                 "valueId": "260865",
                                 "valueName": "billing-saas-1.0"
                             },
                             {
                                 "valueId": "262356",
                                 "valueName": "DevOpsReleaseJan2023"
                             },
                             {
                                 "valueId": "264926",
                                 "valueName": "Disney Release"
                             },
                             {
                                 "valueId": "265040",
                                 "valueName": "service-ui-5.8.0"
                             },
                             {
                                 "valueId": "265068",
                                 "valueName": "service-api-5.8.0"
                             },
                             {
                                 "valueId": "265470",
                                 "valueName": "migrations-5.9.0"
                             },
                             {
                                 "valueId": "265471",
                                 "valueName": "commons-dao-5.9.0"
                             },
                             {
                                 "valueId": "265472",
                                 "valueName": "commons-model-5.8.1"
                             },
                             {
                                 "valueId": "265516",
                                 "valueName": "service-api-5.9.0"
                             },
                             {
                                 "valueId": "265517",
                                 "valueName": "service-authorization-5.8.1"
                             },
                             {
                                 "valueId": "265524",
                                 "valueName": "service-ui-5.9.0"
                             },
                             {
                                 "valueId": "265525",
                                 "valueName": "service-jobs-5.8.1"
                             },
                             {
                                 "valueId": "265716",
                                 "valueName": "commons-dao-5.9.2"
                             },
                             {
                                 "valueId": "265717",
                                 "valueName": "service-jobs-5.8.2"
                             },
                             {
                                 "valueId": "265718",
                                 "valueName": "service-authorization-5.8.2"
                             },
                             {
                                 "valueId": "265719",
                                 "valueName": "service-api-5.9.1"
                             },
                             {
                                 "valueId": "265836",
                                 "valueName": "service-api-5.9.2"
                             },
                             {
                                 "valueId": "265848",
                                 "valueName": "Bitly"
                             },
                             {
                                 "valueId": "265957",
                                 "valueName": "migrations-5.10.0"
                             },
                             {
                                 "valueId": "265961",
                                 "valueName": "commons-dao-5.10.0"
                             },
                             {
                                 "valueId": "265963",
                                 "valueName": "commons-model-5.10.0"
                             },
                             {
                                 "valueId": "266084",
                                 "valueName": "service-ui-5.10.0"
                             },
                             {
                                 "valueId": "266242",
                                 "valueName": "service-jobs-5.10.0"
                             },
                             {
                                 "valueId": "266253",
                                 "valueName": "service-api-5.10.0"
                             },
                             {
                                 "valueId": "266254",
                                 "valueName": "service-authorization-5.10.0"
                             },
                             {
                                 "valueId": "266255",
                                 "valueName": "service-auto-analyzer-5.10.0"
                             },
                             {
                                 "valueId": "270921",
                                 "valueName": "RP-24.1"
                             },
                             {
                                 "valueId": "271023",
                                 "valueName": "commons-model-5.11.0"
                             },
                             {
                                 "valueId": "271025",
                                 "valueName": "migrations-5.11.0"
                             },
                             {
                                 "valueId": "271026",
                                 "valueName": "commons-dao-5.11.0"
                             },
                             {
                                 "valueId": "271028",
                                 "valueName": "service-api-5.11.0"
                             },
                             {
                                 "valueId": "271029",
                                 "valueName": "commons-model-5.11.1"
                             },
                             {
                                 "valueId": "271031",
                                 "valueName": "commons-dao-5.11.1"
                             },
                             {
                                 "valueId": "271032",
                                 "valueName": "service-authorization-5.11.0"
                             },
                             {
                                 "valueId": "271033",
                                 "valueName": "service-auto-analyzer-5.11.0"
                             },
                             {
                                 "valueId": "271035",
                                 "valueName": "service-ui-5.11.0"
                             },
                             {
                                 "valueId": "271251",
                                 "valueName": "RP-24.2"
                             },
                             {
                                 "valueId": "271563",
                                 "valueName": "AgentRelease2024"
                             },
                             {
                                 "valueId": "271564",
                                 "valueName": "DesignRelease2024"
                             },
                             {
                                 "valueId": "271565",
                                 "valueName": "DevOpsRelease2024"
                             },
                             {
                                 "valueId": "271566",
                                 "valueName": "PerfBoardRelease2024"
                             },
                             {
                                 "valueId": "271570",
                                 "valueName": "commons-dao-5.11.2"
                             },
                             {
                                 "valueId": "274907",
                                 "valueName": "CU-24.1.1"
                             },
                             {
                                 "valueId": "276416",
                                 "valueName": "RP-24.3"
                             },
                             {
                                 "valueId": "276673",
                                 "valueName": "RP-25.1"
                             },
                             {
                                 "valueId": "295207",
                                 "valueName": "migrations-5.12.0"
                             },
                             {
                                 "valueId": "295320",
                                 "valueName": "service-jobs-5.12.0"
                             },
                             {
                                 "valueId": "295331",
                                 "valueName": "service-api-5.12.0"
                             },
                             {
                                 "valueId": "295333",
                                 "valueName": "service-authorization-5.12.0"
                             },
                             {
                                 "valueId": "295340",
                                 "valueName": "service-ui-5.12.0"
                             },
                             {
                                 "valueId": "298358",
                                 "valueName": "CU-24.2.1"
                             },
                             {
                                 "valueId": "310231",
                                 "valueName": "service-api-5.12.1"
                             },
                             {
                                 "valueId": "310389",
                                 "valueName": "CU-24.2.2"
                             },
                             {
                                 "valueId": "310390",
                                 "valueName": "CU-24.2.3"
                             },
                             {
                                 "valueId": "316740",
                                 "valueName": "service-api-5.13.0"
                             },
                             {
                                 "valueId": "316742",
                                 "valueName": "service-authorization-5.13.0"
                             },
                             {
                                 "valueId": "316743",
                                 "valueName": "service-jobs-5.13.0"
                             },
                             {
                                 "valueId": "316745",
                                 "valueName": "service-auto-analyzer-5.13.0"
                             },
                             {
                                 "valueId": "319781",
                                 "valueName": "service-ui-5.12.1"
                             },
                             {
                                 "valueId": "324953",
                                 "valueName": "service-api-5.13.1"
                             },
                             {
                                 "valueId": "324958",
                                 "valueName": "service-metrics-gatherer-5.13.0"
                             },
                             {
                                 "valueId": "327772",
                                 "valueName": "RP-25.2"
                             },
                             {
                                 "valueId": "328143",
                                 "valueName": "API"
                             },
                             {
                                 "valueId": "328298",
                                 "valueName": "service-ui-5.12.2"
                             },
                             {
                                 "valueId": "328742",
                                 "valueName": "service-api-5.13.2"
                             },
                             {
                                 "valueId": "329349",
                                 "valueName": "service-ui-5.12.3"
                             },
                             {
                                 "valueId": "329355",
                                 "valueName": "RP-25.3"
                             },
                             {
                                 "valueId": "332002",
                                 "valueName": "service-jobs-5.13.1"
                             },
                             {
                                 "valueId": "332003",
                                 "valueName": "service-authorization-5.13.1"
                             },
                             {
                                 "valueId": "332004",
                                 "valueName": "service-api-5.13.3"
                             },
                             {
                                 "valueId": "335260",
                                 "valueName": "CU-25.0.4"
                             },
                             {
                                 "valueId": "335274",
                                 "valueName": "service-api-5.13.4"
                             },
                             {
                                 "valueId": "335410",
                                 "valueName": "service-auto-analyzer-5.13.2"
                             },
                             {
                                 "valueId": "336087",
                                 "valueName": "service-ui-5.12.4"
                             },
                             {
                                 "valueId": "336240",
                                 "valueName": "CU-25.0.5"
                             },
                             {
                                 "valueId": "336248",
                                 "valueName": "service-api-5.13.5"
                             }
                         ],
                         "value": [
                             "RP-25.2"
                         ]
                     },
                     {
                         "id": "description",
                         "required": false,
                         "fieldName": "Description",
                         "fieldType": "string",
                         "definedValues": [],
                         "value": [
                             "hello world"
                         ]
                     },
                     {
                         "id": "customfield_10004",
                         "required": false,
                         "fieldName": "Story Points",
                         "fieldType": "number",
                         "definedValues": [],
                         "value": [
                             "13"
                         ]
                     },
                     {
                         "id": "labels",
                         "required": false,
                         "fieldName": "Labels",
                         "fieldType": "array",
                         "definedValues": [],
                         "value": [
                             "CU-24.2.3"
                         ]
                     },
                     {
                         "id": "customfield_10102",
                         "required": false,
                         "fieldName": "Environment:",
                         "fieldType": "array",
                         "definedValues": [
                             {
                                 "valueId": "27920",
                                 "valueName": "dev7776"
                             },
                             {
                                 "valueId": "27921",
                                 "valueName": "Production"
                             },
                             {
                                 "valueId": "27922",
                                 "valueName": "Staging"
                             },
                             {
                                 "valueId": "27923",
                                 "valueName": "TR stag"
                             },
                             {
                                 "valueId": "27924",
                                 "valueName": "TR VLAN"
                             },
                             {
                                 "valueId": "31126",
                                 "valueName": "OS 6293"
                             },
                             {
                                 "valueId": "31127",
                                 "valueName": "EPAM 6293"
                             },
                             {
                                 "valueId": "37102",
                                 "valueName": "Dev:8080"
                             },
                             {
                                 "valueId": "37103",
                                 "valueName": "Dev:9090"
                             },
                             {
                                 "valueId": "37104",
                                 "valueName": "RP_AWS"
                             },
                             {
                                 "valueId": "37105",
                                 "valueName": "RP_DEMO"
                             },
                             {
                                 "valueId": "37106",
                                 "valueName": "Localhost"
                             },
                             {
                                 "valueId": "37948",
                                 "valueName": "RP_BETA"
                             }
                         ],
                         "value": [
                             "Dev:8080"
                         ]
                     },
                     {
                         "id": "customfield_12200",
                         "required": false,
                         "fieldName": "Customer Email",
                         "fieldType": "string",
                         "definedValues": [],
                         "value": [
                             "hello from tester"
                         ]
                     },
                     {
                         "id": "customfield_13500",
                         "value": [
                             "blocker"
                         ],
                         "required": false,
                         "fieldName": "Severity.",
                         "fieldType": "option",
                         "definedValues": [
                             {
                                 "valueId": "None",
                                 "valueName": "None"
                             },
                             {
                                 "valueId": "11400",
                                 "valueName": "normal"
                             },
                             {
                                 "valueId": "11419",
                                 "valueName": "blocker"
                             },
                             {
                                 "valueId": "11420",
                                 "valueName": "minor"
                             },
                             {
                                 "valueId": "11422",
                                 "valueName": "major"
                             },
                             {
                                 "valueId": "11424",
                                 "valueName": "critical"
                             },
                             {
                                 "valueId": "11425",
                                 "valueName": "trivial"
                             }
                         ]
                     },
                     {
                         "id": "customfield_13206",
                         "required": false,
                         "fieldName": "Steps to Reproduce",
                         "fieldType": "string",
                         "definedValues": [],
                         "value": [
                             "1Step 2Step 3Step"
                         ]
                     }
                 ],
                 "backLinks": {
                     "342243": "http://dev.epmrpp.reportportal.io/ui/#helen/launches/all/89343/342241/342242/342243/log?item0Params=filter.eq.hasStats%3Dtrue%26filter.eq.hasChildren%3Dfalse%26filter.in.type%3DSTEP%26filter.in.status%3DFAILED%252CINTERRUPTED"
                 }
             }
      """;

}
