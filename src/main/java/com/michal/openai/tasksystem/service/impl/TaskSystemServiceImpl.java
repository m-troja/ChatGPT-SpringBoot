package com.michal.openai.tasksystem.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.exception.TaskSystemException;
import com.michal.openai.tasksystem.entity.TaskSystemUserDto;
import com.michal.openai.tasksystem.entity.request.CreateTaskSystemIssueRequest;
import com.michal.openai.tasksystem.entity.request.TaskSystemLoginRequest;
import com.michal.openai.tasksystem.entity.request.TaskSystemRegisterRequest;
import com.michal.openai.tasksystem.entity.response.TaskSystemIssueDto;
import com.michal.openai.tasksystem.entity.response.TaskSystemTokenResponse;
import com.michal.openai.tasksystem.service.TaskSystemService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.CompletionException;

@Data
@Slf4j
@Service
public class TaskSystemServiceImpl implements TaskSystemService {

    private String getAllIssuesEndpoint = "/api/v1/issue/all";
    private String createIssueEndpoint = "/api/v1/chatgpt/issue/create";
    private String assignIssueEndpoint = "/api/v1/chatgpt/issue/assign";
    private String getUserBySlackUserIdEndpoint = "/api/v1/chatgpt/user/slack-user-id";
    private String loginEndpoint = "/api/v1/login";
    private String registerEndpoint = "/api/v1/register";
    private String botSlackUserId = "USLACKBOT";
    private String botEmail = "test@test.com";
    private String botPassword = "StrongBotPassword";
    private int attempts = 3;

    @Qualifier("taskSystemRestClient")
    private final RestClient restClient;

    public TaskSystemServiceImpl( @Qualifier("taskSystemRestClient") RestClient restClient) {
        this.restClient = restClient;
    }
    //   Send "CreateIssue" request to Task-System
    @Override
    public TaskSystemIssueDto createIssue(String requestBody) {
        TaskSystemIssueDto issueDtoResponse;
        log.debug("Inside createIssue with requestBody: {}", requestBody);
            try
            {
                log.debug("Serialize GPT-request to Task-System-Request-JSON");
                //Serialize GPT-request to Task-System-Request-JSON
                CreateTaskSystemIssueRequest createIssueRequest = new ObjectMapper()
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .readValue(requestBody, CreateTaskSystemIssueRequest.class);

                // Push Task-System-Request-JSON to Task-System-API
                log.debug("Rest Client: Push Task-System-Request-JSON to Task-System-API");
                issueDtoResponse =
                        restClient.post()
                        .uri(createIssueEndpoint)
                        .body(createIssueRequest)
                        .retrieve()
                        .body(TaskSystemIssueDto.class);
                log.debug("Rest client \"createIssue\" response from Task-System: {}", issueDtoResponse);
            }
            catch (JsonProcessingException e)
            {
                log.error("Failed to serialize create issue request to JSON, {}", e.getMessage());
                throw new CompletionException(e);
            }
            catch (Exception e)
            {
                log.error("Error creating issue: {}", e.getMessage());
                throw new CompletionException(e);
            }
            return issueDtoResponse;
    }

    @Override
    public TaskSystemIssueDto assignIssue(String requestBody) {
        log.debug("Assign issue RequestBody to Task-System: {}", requestBody);
        var issueDto = restClient.put()
                .uri(assignIssueEndpoint)
                .body(requestBody)
                .retrieve()
                .body(TaskSystemIssueDto.class);
        log.debug("Rest client \"assignIssue\" response from Task-System: {}", issueDto);
        return issueDto;
    }

    public List<TaskSystemIssueDto> getAllIssues() {

        try {
            List<TaskSystemIssueDto> issues = restClient.get()
                    .uri(getAllIssuesEndpoint)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>(){});
            if (issues != null) { log.info("Fetched {} issues from Task-System", issues.size());}
            else { log.debug("Fetched 0 issues from Task-System");}

            log.debug("Rest client \"getAllIssues\" response from Task-System: {}", issues);
            return issues;
        }
        catch (Exception e) {
            log.error("Error fetching issues from Task-System: {}", e.getMessage());
            throw new RuntimeException("Error fetching issues from Task-System");
        }
    }

    @Override
    public TaskSystemUserDto getTaskSystemUser(String username) {
        log.debug("Getting task-system user: {}", username);
        try {
            var userDto = restClient.get()
                    .uri(getUserBySlackUserIdEndpoint + "/" + username)
                    .retrieve()
                    .body(TaskSystemUserDto.class);
            log.debug("User from Task-System: {}", userDto);
            return userDto;
        }
        // WHEN API RETURNS SC_401
        catch (HttpClientErrorException.Unauthorized exUnauthorized) {
            log.error("Caught Unauthorized Exception, trying to login into Task-System");
            throw new RuntimeException("Error connecting to Task-System");
        }
        catch (Exception e) {
            log.error("Error connecting to Task-System", e);
            throw new RuntimeException("Error connecting to Task-System");
        }
    }

    @Override
    public TaskSystemUserDto getTaskSystemUserAuthorized(String username) {
        log.debug("Getting Authorized task-system user: {}", username);
        int i;
        for (i = 1; i <= attempts; i++)
        {
            var tokens = login();
            try {
            var userDto = restClient.get()
                    .uri(getUserBySlackUserIdEndpoint + "/" + username)
                    .header("Authorization", "Bearer " + tokens.accessToken())
                    .retrieve()
                    .body(TaskSystemUserDto.class);
            log.debug("Authorized User from Task-System: {}", userDto);
            return userDto;
            }
            // WHEN API RETURNS SC_401
            catch (HttpClientErrorException.Unauthorized exUnauthorized) {
                if (i < attempts) {
                    log.error("Authorized Caught Unauthorized Exception, trying to login into Task-System, attempt {}/{}", i, attempts,
                            exUnauthorized);
                }
                else {
                    log.error("Error connecting to Task-System, attempt {}/{}", i, attempts, exUnauthorized);
                    throw new TaskSystemException("Error connecting to Task-System");

                }
            }
            catch (Exception e) {
                log.error("Error connecting to Task-System, attempt {}/{}", i, attempts, e);
                throw new TaskSystemException("Error connecting to Task-System");
            }
        }
        return null;
    }

    private TaskSystemTokenResponse login() {
        log.debug("Logging into Task-System with username: {}", botSlackUserId);
        var req = new TaskSystemLoginRequest(botEmail, botPassword);
        try {
            var tokens = restClient.post()
                    .uri(loginEndpoint)
                    .body(req)
                    .retrieve()
                    .body(TaskSystemTokenResponse.class);
            log.debug("Tokens: {}", tokens);
            return tokens;
        }
        // WHEN API RETURNS SC_400
        catch (HttpClientErrorException.BadRequest exBadRequest) {
                log.error("Failed to login into Task-System, trying to register...", exBadRequest);
                var userDto = register();
        }
        catch (Exception e) {
            log.error("Error logging into Task-System", e);
            throw new TaskSystemException("Error logging into Task-System");
        }
        throw new TaskSystemException("Failed to login into Task-System");
    }

    private TaskSystemUserDto register() {
        log.debug("Registering into Task-system with username: {}", botSlackUserId);
        var req = new TaskSystemRegisterRequest("Slack", "Bot", botEmail, botPassword );
        try {
            var userDto = restClient.post()
                    .uri(registerEndpoint)
                    .body(req)
                    .retrieve()
                    .body(TaskSystemUserDto.class);
            log.debug("Registered successfull. DTO from Task-System: {}", userDto);
            return userDto;
        } catch (Exception e) {
            log.error("Error during registration", e);
            throw new TaskSystemException("Error during registration");
        }
    }
}
