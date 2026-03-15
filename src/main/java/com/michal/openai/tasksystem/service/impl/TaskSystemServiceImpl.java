package com.michal.openai.tasksystem.service.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.michal.openai.exception.TaskSystemException;
import com.michal.openai.exception.TaskSystemLoginException;
import com.michal.openai.gpt.tool.tools.AssignTaskSystemIssueTool;
import com.michal.openai.gpt.tool.tools.CreateTaskSystemIssueTool;
import com.michal.openai.persistence.SlackRepo;
import com.michal.openai.slack.SlackService;
import com.michal.openai.tasksystem.entity.TaskSystemEventType;
import com.michal.openai.tasksystem.entity.dto.TaskSystemIssueDto;
import com.michal.openai.tasksystem.entity.dto.TaskSystemUserDto;
import com.michal.openai.tasksystem.entity.request.CreateTaskSystemIssueRequest;
import com.michal.openai.tasksystem.entity.request.TaskSystemLoginRequest;
import com.michal.openai.tasksystem.entity.request.TaskSystemRegisterRequest;
import com.michal.openai.tasksystem.entity.response.TaskSystemEvent;
import com.michal.openai.tasksystem.entity.response.TaskSystemTokenResponse;
import com.michal.openai.tasksystem.entity.token.TokenStore;
import com.michal.openai.tasksystem.service.TaskSystemService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.CompletionException;

import static com.michal.openai.tasksystem.entity.TaskSystemEventType.valueOf;

@Slf4j
@Service
public class TaskSystemServiceImpl implements TaskSystemService {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final TokenStore tokenStore;

    private static final String GET_ALL_ISSUES_ENDPOINT = "/api/v1/issue/all";
    private static final String GET_ISSUES_FOR_USER_ENDPOINT = "/api/v1/chatgpt/issues/slack-user-id";
    private static final String CREATE_ISSUE_ENDPOINT = "/api/v1/chatgpt/issue/create";
    private static final String ASSIGN_ISSUE_ENDPOINT = "/api/v1/chatgpt/issue/assign";
    private static final String GET_USER_BY_SLACK_ENDPOINT = "/api/v1/chatgpt/user/slack-user-id";
    private static final String LOGIN_ENDPOINT = "/api/v1/login";
    private static final String REGISTER_ENDPOINT = "/api/v1/register";

    @Value("${TS_BOT_SLACK_USER_ID}")
    private String BOT_SLACK_USER_ID;

    @Value("${TS_BOT_EMAIL}")
    private String BOT_EMAIL;

    @Value("${TS_BOT_PW}")
    private String BOT_PASSWORD;

    private static final String LINK_PREFIX = "http://komuna.site/issues/";
    private final String slackChannelId;

    private static final int ATTEMPTS = 3;
    private final SlackService slackService;
    private final SlackRepo slackRepo;

    @Qualifier("taskSystemRestClient") private final RestClient restClient;



    public TaskSystemServiceImpl(TokenStore tokenStore, SlackService slackService, @Qualifier("taskSystemRestClient") RestClient restClient,
                                 SlackRepo slackRepo) {
        this.tokenStore = tokenStore;
        this.slackService = slackService;
        this.restClient = restClient;
        this.slackRepo = slackRepo;
        this.slackChannelId =
                System.getenv().getOrDefault("SLACK_CHANNEL_ID", "C08RLDBCRB9");

    }

    @Override
    public TaskSystemIssueDto createIssue(CreateTaskSystemIssueTool.Args args) {
        log.debug("Creating issue, args: {}", args);

        log.debug("req: {}", objectMapper.convertValue( args, CreateTaskSystemIssueRequest.class ) );

        if (tokenStore.isExpired()) {
            login();
        }
        try {
            CreateTaskSystemIssueRequest createIssueRequest = objectMapper.convertValue(
                     args, CreateTaskSystemIssueRequest.class
            );
            TaskSystemIssueDto response = restClient.post()
                    .uri(CREATE_ISSUE_ENDPOINT)
                    .header("Authorization", "Bearer " + tokenStore.getAccessToken())
                    .body(createIssueRequest)
                    .retrieve()
                    .body(TaskSystemIssueDto.class);
            log.debug("Created issue response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error creating issue: {}", e.getMessage(), e);
            throw new CompletionException(e);
        }
    }

    @Override
    public TaskSystemIssueDto assignIssue(AssignTaskSystemIssueTool.Args requestBody) {
        if (tokenStore.isExpired()) {
            login();
        }
        try {
            TaskSystemIssueDto response = restClient.put()
                    .uri(ASSIGN_ISSUE_ENDPOINT)
                    .header("Authorization", "Bearer " + tokenStore.getAccessToken())
                    .body(requestBody)
                    .retrieve()
                    .body(TaskSystemIssueDto.class);
            log.debug("Assigned issue response: {}", response);
            return response;
        } catch (Exception e) {
            log.error("Error assigning issue: {}", e.getMessage(), e);
            throw new CompletionException(e);
        }
    }

    @Override
    public List<TaskSystemIssueDto> getAllIssues() {
        if (tokenStore.isExpired()) {
            login();
        }
        try {
            List<TaskSystemIssueDto> issues = restClient.get()
                    .uri(GET_ALL_ISSUES_ENDPOINT)
                    .header("Authorization", "Bearer " + tokenStore.getAccessToken())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            log.debug("Fetched issues: {}", issues);
            return issues;
        } catch (Exception e) {
            log.error("Error fetching all issues: {}", e.getMessage(), e);
            throw new TaskSystemException("Error fetching issues from Task-System", e);
        }
    }
    @Override
    public List<TaskSystemIssueDto> getIssuesForUser(String slackUserId) {


        String uri = GET_ISSUES_FOR_USER_ENDPOINT + "/" + slackUserId;

        log.debug("getIssuesForUser: {}, uri: {}", slackUserId, uri
        );
        if (tokenStore.isExpired()) {
            login();
        }
        try {
            List<TaskSystemIssueDto> issues = restClient.get()
                    .uri(uri)
                    .header("Authorization", "Bearer " + tokenStore.getAccessToken())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            log.debug("Fetched issues for user: {}", issues);
            return issues;
        } catch (Exception e) {
            log.error("Error fetching all issues: {}", e.getMessage(), e);
            throw new TaskSystemException("Error fetching issues from Task-System", e);
        }
    }


    @Override
    public TaskSystemUserDto getTaskSystemUser(String username) {
        if (tokenStore.isExpired()) {
            login();
        }
        try {
            TaskSystemUserDto user = restClient.get()
                    .uri(GET_USER_BY_SLACK_ENDPOINT + "/" + username)
                    .header("Authorization", "Bearer " + tokenStore.getAccessToken())
                    .retrieve()
                    .body(TaskSystemUserDto.class);
            log.debug("Fetched user: {}", user);
            return user;
        } catch (HttpClientErrorException.Unauthorized ex) {
            log.error("Unauthorized fetching user {}", username);
            throw new TaskSystemException("Unauthorized to Task-System");
        } catch (Exception e) {
            log.error("Error fetching user {}", username, e);
            throw new TaskSystemException("Error connecting to Task-System", e);
        }
    }

    @Override
    public TaskSystemUserDto getTaskSystemUserAuthorized(String username) {
        if (tokenStore.isExpired()) {
            login();
        }
        try {
            return restClient.get()
                    .uri(GET_USER_BY_SLACK_ENDPOINT + "/" + username)
                    .header("Authorization", "Bearer " + tokenStore.getAccessToken())
                    .retrieve()
                    .body(TaskSystemUserDto.class);

        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("Unauthorized to get user");
            throw new TaskSystemException("Unauthorized to get user");
        } catch (Exception e) {
            throw new TaskSystemException("Error fetching authorized user", e);
        }
    }


    private void login() {
        String uri = LOGIN_ENDPOINT;
        log.debug("Logging into TaskSystem: email: {}, endpoint: {}", BOT_EMAIL, uri);

        var request = new TaskSystemLoginRequest(BOT_EMAIL, BOT_PASSWORD);

        for (int attempt = 1; attempt <= ATTEMPTS; attempt++) {
            try {
                log.debug("Login attempt {}", attempt);

                TaskSystemTokenResponse tokens = restClient.post()
                        .uri(uri)
                        .body(request)
                        .retrieve()
                        .body(TaskSystemTokenResponse.class);

                if (tokens == null || tokens.accessToken() == null || tokens.accessToken().token().isEmpty()) {
                    throw new TaskSystemLoginException("Failed to retrieve access token");
                }

                tokenStore.setAccessToken(tokens.accessToken().token());
                tokenStore.setExpiresAt(tokens.accessToken().expires());
                log.debug("Login successful, access token acquired");
                return; // success, exit method

            } catch (HttpClientErrorException.Unauthorized | HttpClientErrorException.BadRequest e) {
                log.warn("Login failed on attempt {}: {}, trying to register bot…", attempt, e.getMessage());
                register(); // attempt registration
            } catch (Exception e) {
                log.error("Unexpected error on login attempt {}: {}", attempt, e.getMessage(), e);
            }
        }

        throw new TaskSystemLoginException("Login failed after " + ATTEMPTS + " attempts");
    }

    private void register() {
        String uri = REGISTER_ENDPOINT;
        log.debug("Registering bot: email: {}, slackId: {}, endpoint: {}", BOT_EMAIL, BOT_SLACK_USER_ID, uri);

        var req = new TaskSystemRegisterRequest("Slack", "Bot", BOT_EMAIL, BOT_PASSWORD, BOT_SLACK_USER_ID);

        int attempt = 1;
        while (attempt <= ATTEMPTS) {
            try {
                log.debug("Register attempt {}", attempt);

                TaskSystemUserDto user = restClient.post()
                        .uri(uri)
                        .body(req)
                        .retrieve()
                        .body(TaskSystemUserDto.class);

                log.debug("Bot registered successfully: {}", user);
                return; // success
            } catch (Exception e) {
                log.warn("Attempt {} failed", attempt);
            }
            attempt++;
        }
        throw new TaskSystemLoginException("Login failed after " + ATTEMPTS + " attempts");
    }

    public void parseTaskSystemEvent(TaskSystemEvent event) {
        log.info("Received event {}: ", event);
        TaskSystemEventType eventType;

        try {
            eventType = valueOf(event.event().toString().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Invalid event type: {}", event.event());
            throw new IllegalArgumentException("Invalid event type: " + event.event());
        }

        var assignee = slackRepo.findBySlackUserId(event.issue().assigneeSlackId());
        var author = slackRepo.findBySlackUserId(event.issue().authorSlackId());
        var comments = event.issue().comments();

        String comment = (comments != null && !comments.isEmpty())
                ? comments.getLast().content()
                : "comment";

        var message = switch (eventType) {
            case CREATED_ISSUE -> {
                if (assignee != null && author != null) {
                    yield  String.format("Hey <@%s>, a new ticket for you has been created by <@%s>! Title: %s [%s]", assignee.getSlackUserId(), author.getSlackUserId(), event.issue().title(), LINK_PREFIX + event.issue().id());
                } else if (assignee == null) {
                    yield String.format("New ticket has been created by <@%s>! Title: %s [%s]", author.getSlackUserId(), event.issue().title(), LINK_PREFIX + event.issue().id());
                } else {
                    yield String.format("New ticket has been created! Title: %s [%s]", event.issue().title(),  LINK_PREFIX + event.issue().id() );
                }
            }
            case UPDATED_ASSIGNEE ->
                 String.format("Hey <@%s>, a new issue has been assigned to you by <@%s>! Title: %s, [%s]", assignee.getSlackUserId(), event.eventUserSlackId() , event.issue().title(), LINK_PREFIX + event.issue().id());
            case DELETED_ISSUE -> String.format("Issue %s has been deleted by <@%s>", event.issue().key(), event.eventUserSlackId());
            case CREATED_COMMENT ->
                 String.format("New comment by <@%s> posted in %s: %s [%s]", event.eventUserSlackId(), event.issue().key(), comment,  LINK_PREFIX + event.issue().id());
            case UPDATED_PRIORITY ->
                 String.format("Priority of %s was set to %s by <@%s> [%s]", event.issue().key(), event.issue().priority(), event.eventUserSlackId(), LINK_PREFIX + event.issue().id());
            case UPDATED_STATUS ->
                String.format("Status of %s was set to %s by <@%s> [%s]", event.issue().key(), event.issue().status(), event.eventUserSlackId(), LINK_PREFIX + event.issue().id());
            case UPDATED_TEAM ->
                    String.format("Team of %s was set to %s by <@%s> [%s]", event.issue().key(), event.issue().teamName(), event.eventUserSlackId(), LINK_PREFIX + event.issue().id());
            case UPDATED_TITLE ->
                    String.format("Title of %s was set to %s by <@%s> [%s]", event.issue().key(), event.issue().title(), event.eventUserSlackId(), LINK_PREFIX + event.issue().id());
            case UPDATED_DESCRIPTION ->
                    String.format("Description of %s was set to %s by <@%s> [%s]", event.issue().key(), event.issue().description(), event.eventUserSlackId(), LINK_PREFIX + event.issue().id());
            case UPDATED_DUEDATE ->
                    String.format("Due-date of %s was set to %s by <@%s> [%s]", event.issue().key(), event.issue().dueDate(), event.eventUserSlackId(), LINK_PREFIX + event.issue().id());

        };
        slackService.sendMessageToSlack(message, slackChannelId);
    }
}
