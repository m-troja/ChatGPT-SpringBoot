# ChatGPT Integration

**ChatGPT Integration Platform** is a Spring Boot-based web application that integrates OpenAI's ChatGPT with **Slack** and **Jira** APIs. Designed as a modular and extensible backend, this application facilitates AI-powered task automation, conversational interfaces, and project management workflows.

---

## Features

- **Slack Integration**: Chatbot responds to Slack mentions using OpenAI API or performs tasks via function calls.
- **Jira Integration**: Retrieve ticket info and create new issues based on user requests in Slack channels.
- **Admin Endpoint**: Clear the database if needed.
- **Tracking**: Requests and responses are assigned to specific Slack users and stored in the database.
- **OpenAPI Documentation**: Available at `/swagger`.

---

## Slack Integration

- REST API endpoint: `/api/v1/slack`
- Listens for Slack mention events and responds automatically using ChatGPT.
- Parses incoming Slack payloads and triggers processing via `SlackService`.

---

## Jira Integration

- Retrieve a single issue: `GET /v1/jira/issue?id=ISSUE-ID`
- List project-specific issues: `GET /v1/jira/issues-java`
- Create new issues programmatically: `POST /v1/jira/create-issue`
- All Jira interactions are asynchronous and optimized for speed and scalability.

---

## Task-System Integration

- Create and assign tickets in another application: [Task-System](https://github.com/m-troja/Task-System)
- JWT Authentication between applications

---

## Technology Stack

| Component                  | Details              |
|----------------------------|----------------------|
| **Language**               | Java 21              |
| **Framework**              | Spring Boot 3.5.7    |
| **Build Tool**             | Maven                |
| **Logging**                | SLF4J with Lombok    |
| **JSON Mapper**            | Jackson ObjectMapper |
| **Unit tests**             | JUnit, Mockito       |
| **Custom app integration** | Task-System          |

---

## Requirements

- Public static IP address
- Jira account and API token
- OpenAI account and API token
- Slack account and API token, SlackBot installed into workspace

---

## Configuration

### Secrets (`src/main/resources/secrets.properties`)

```properties
gpt.chat.api.key=
slack.bot.oauth.token=
jira.key=
github.token=
```

Database and Jira config is stored in application.properties.

### Env vars

App saves incoming and outgoing JSON files. Env variables needed to configure file path:
```
CHAT_LOG_DIR=C:\\tmp\\log
CHAT_JSON_DIR=C:\\tmp\\JSON
CHAT_LOG_FILENAME=chatgpt
```
Other required env vars (with example values):
```
GPT_CHAT_SYSTEM_INITIAL_MESSAGE=You are a slack-bot
CHAT_SERVER_ADDRESS=0.0.0.0
CHAT_SERVER_PORT=6969

CHAT_DB_HOST=localhost
CHAT_DB_NAME=chatgpt-integration
CHAT_DB_Port=5432
CHAT_DB_USERNAME=chatgpt
CHAT_DB_PASSWORD=chatgptPW

CHAT_MODEL=gpt-5.1
CHAT_MAX_TOKENS=4240

TS_HOST=localhost
TS_HTTP_PORT=6901
```

