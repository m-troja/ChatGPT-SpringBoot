package com.michal.openai.tasksystem.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.michal.openai.exception.TaskSystemException;
import com.michal.openai.exception.TaskSystemLoginException;
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
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.config.Task;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.CompletionException;

import static com.michal.openai.tasksystem.entity.TaskSystemEventType.*;

@Slf4j
@Service
public class TaskSystemServiceImpl implements TaskSystemService {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final TokenStore tokenStore;

    private static final String GET_ALL_ISSUES_ENDPOINT = "/api/v1/issue/all";
    private static final String CREATE_ISSUE_ENDPOINT = "/api/v1/chatgpt/issue/create";
    private static final String ASSIGN_ISSUE_ENDPOINT = "/api/v1/chatgpt/issue/assign";
    private static final String GET_USER_BY_SLACK_ENDPOINT = "/api/v1/chatgpt/user/slack-user-id";
    private static final String LOGIN_ENDPOINT = "/api/v1/login";
    private static final String REGISTER_ENDPOINT = "/api/v1/register";

    private static final String BOT_SLACK_USER_ID = "USLACKBOT";
    private static final String BOT_EMAIL = "test@test.com";
    private static final String BOT_PASSWORD = "StrongBotPassword";
    private static final String LINK_PREFIX = "http://komuna.site/issue/";

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
    }

    @Override
    public TaskSystemIssueDto createIssue(String requestBody) {
        if (tokenStore.isExpired()) {
            login();
        }
        try {
            CreateTaskSystemIssueRequest createIssueRequest = objectMapper.readValue(
                    requestBody, CreateTaskSystemIssueRequest.class
            );
            TaskSystemIssueDto response = restClient.post()
                    .uri(CREATE_ISSUE_ENDPOINT)
                    .header("Authorization", "Bearer " + tokenStore.getAccessToken())
                    .body(createIssueRequest)
                    .retrieve()
                    .body(TaskSystemIssueDto.class);
            log.debug("Created issue response: {}", response);
            return response;
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize create issue request: {}", e.getMessage());
            throw new CompletionException(e);
        } catch (Exception e) {
            log.error("Error creating issue: {}", e.getMessage(), e);
            throw new CompletionException(e);
        }
    }

    @Override
    public TaskSystemIssueDto assignIssue(String requestBody) {
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
        var request = new TaskSystemLoginRequest(BOT_EMAIL, BOT_PASSWORD);
            for (int i = 1; i <= ATTEMPTS; i++) {
                try {
                    log.debug("Login attempt {}", i);
                    var tokensString = restClient.post()
                            .uri(LOGIN_ENDPOINT)
                            .body(request)
                            .retrieve()
                            .body(String.class);
                    log.debug("[LOGIN] Raw rest client response:");
                    log.debug("{}", tokensString);
                    TaskSystemTokenResponse tokens = null;
                    try {
                        tokens = objectMapper.readValue(tokensString, TaskSystemTokenResponse.class);
                    } catch (JsonProcessingException e) {
                        log.debug("Error processing tokens:", e);
                        throw new RuntimeException(e);
                    }
                    log.debug("Tokens RestClient response: {}", tokens);
                    if (tokens == null || tokens.accessToken() == null || tokens.accessToken().token().isEmpty()) {
                        throw new TaskSystemLoginException("Failed to retrieve access token");
                    }
                    tokenStore.setAccessToken(tokens.accessToken().token());
                    tokenStore.setExpiresAt(tokens.accessToken().expires() );
                    return;

                } catch (HttpClientErrorException.BadRequest |
                         HttpClientErrorException.Unauthorized e) {
                    log.warn("Login failed, registering bot…");
                    register();
                } catch (Exception e) {
                    if (i == ATTEMPTS)
                        throw new TaskSystemLoginException("Login failed after retries");
                }
            }

            throw new TaskSystemLoginException("Login failed after retries");
        }

    private void register() {
        TaskSystemRegisterRequest req = new TaskSystemRegisterRequest(
                "Slack", "Bot", BOT_EMAIL, BOT_PASSWORD, BOT_SLACK_USER_ID
        );
        int i;
        for (i = 1; i <= ATTEMPTS; i++) {
            try {
                log.debug("Register attempt {}", i);

                restClient.post()
                        .uri(REGISTER_ENDPOINT)
                        .body(req)
                        .retrieve()
                        .body(TaskSystemUserDto.class);

                return;

            } catch (Exception e) {
                log.debug("Exception after {} attempt: {}", i, e.getMessage());
                if (i == ATTEMPTS) throw new TaskSystemException("Registration failed after retries");
            }
        }
    }

    public void parseTaskSystemEvent(TaskSystemEvent event) {
        log.info("Received event {}: ", event);
        TaskSystemEventType eventType;

        try {
            eventType = TaskSystemEventType.valueOf(event.event().toString().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.error("Invalid event type: " + event.event());
            throw new IllegalArgumentException("Invalid event type: " + event.event());
        }

        var assignee = slackRepo.findBySlackUserId(event.issue().assigneeSlackId());
        var author = slackRepo.findBySlackUserId(event.issue().authorSlackId());
        var message = switch (eventType) {

            case ISSUE_CREATED -> {
                if (assignee != null && author != null) {
//                    yield  String.format("Hey <@%s>, a new ticket for you has been created by <@%s>! Title: %s, Link: %s", assignee.getSlackUserId(), author.getSlackUserId(), event.issue().title(), LINK_PREFIX + event.issue().key());
                    yield  String.format("Hey <@%s>, a new ticket for you has been created by <@%s>! Title: %s, Link: jak Piotr zrobi to kiedyś tu będzie :D", assignee.getSlackUserId(), author.getSlackUserId(), event.issue().title());
                } else if (author != null) {
                    yield String.format("New ticket has been created by <@%s>! Title: %s, Link: %s", event.issue().title(), author.getSlackUserId(), LINK_PREFIX + event.issue().key());
                } else {
                    yield String.format("New ticket has been created! Title: %s, Link: %s", event.issue().title(), LINK_PREFIX + event.issue().key());
                }
            }
            case ISSUE_ASSIGNED ->
//                 String.format("Hey <@%s>, a new issue has been assigned to you! Title: %s, Link: %s", assignee.getSlackUserId(), event.issue().title(),  LINK_PREFIX + event.issue().key());
                 String.format("Hey <@%s>, a new issue has been assigned to you! Title: %s, Link: jak Piotr zrobi to kiedyś tu będzie :D", assignee.getSlackUserId(), event.issue().title());
            case COMMENT_CREATED ->
                 String.format("New comment by %s posted in %s: %s", event.issue().comments().getLast().authorName(), event.issue().key(), event.issue().comments().getLast().content());
            case UPDATE_DUEDATE ->
                 String.format("New due-date of %s: %s", event.issue().key(), event.issue().dueDate());
            case UPDATE_PRIORITY ->
                 String.format("New priority of %s: %s", event.issue().key(), event.issue().priority());
            case UPDATE_STATUS ->
                String.format("Status update for %s: %s", event.issue().key(), event.issue().status());
        };
        slackService.sendMessageToSlack(message, "test");
    }
}
