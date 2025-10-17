# ChatGPT Integration Platform

**ChatGPT Integration Platform** is a Spring Boot-based web application that integrates OpenAI's ChatGPT with **Slack** and **Jira** APIs. Designed as a modular and extensible backend, this application facilitates AI-powered task automation, conversational interfaces, and project management workflows.

---

## Features

- Slack: integration with Slack workspace. Chatbot answer questions over OpenAI API or completes task with function calls.
- Jira integration: retrieve tickets info, post a new ticket according to user requirements written in Slack channel.
- Email: send an email to given user using SMTP credentials.
- Admin-endpoint to clear the database.
- Requests and answers are being assigned to specific Slack user and stored in the database.

### Slack Integration

- REST API endpoint: `/v1/slack`
- Listens for Slack mention events and responds automatically using ChatGPT.
- Parses incoming Slack payloads and triggers processing via `SlackService`.

### Jira Integration

- Retrieve a single issue: `GET /v1/jira/issue?id=ISSUE-ID`
- List project-specific issues: `GET /v1/jira/issues-java`
- Create new issues programmatically: `POST /v1/jira/create-issue`
- All Jira interactions are asynchronous and optimized for speed and scalability.

---

## Technology Stack

| Component       | Details                                |
| --------------- |----------------------------------------|
| **Language**    | Java 21                                |
| **Framework**   | Spring Boot 3.4.1                      |
| **Build Tool**  | Maven                                  |
| **Slack SDK**   | Custom `SlackService` integration      |
| **Jira SDK**    | Custom `JiraService` for RESTful calls |
| **Logging**     | SLF4J with Lombok                      |
| **JSON Mapper** | Jackson ObjectMapper                   |

## Requirements

- public static IP address
- Jira account and API token
- OpenAI account and API token
- Slack account and API token, SlackBot installed into workspace
- public smtp credentials

## Config

Parameters to put into src/main/resources/secrets.properties:

- gpt.chat.api.key=
- slack.bot.oauth.token=
- jira.key=
- mail.sender.email=
- mail.receiver.test.email=
- mail.sender.email.password=

Database and Jira config is stored in application.properties.

## Logging

App saves incoming and outgoing JSON files. Env variables needed to configure file path:

CHAT_LOG_DIR=C:\\tmp\\log
CHAT_JSON_DIR=C:\\tmp\\JSON
CHAT_LOG_FILENAME=chatgpt
