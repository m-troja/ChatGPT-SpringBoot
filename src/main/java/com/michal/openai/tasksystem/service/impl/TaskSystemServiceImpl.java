package com.michal.openai.tasksystem.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.tasksystem.service.TaskSystemService;
import com.michal.openai.tasksystem.entity.request.CreateTaskSystemIssueRequest;
import com.michal.openai.tasksystem.entity.response.TaskSystemIssueDto;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Data
@Slf4j
@Service
public class TaskSystemServiceImpl implements TaskSystemService {

    private String getAllIssuesEndpoint = "/api/v1/issue/all";
    private String createIssueEndpoint = "/api/v1/chatgpt/issue/create";
    private String assignIssueEndpoint = "/api/v1/chatgpt/issue/assign";
    private String getUserBySlackUserIdEndpoint = "/api/v1/chatgpt/user/slack-user-id";

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
    public TaskSystemIssueDto assignIssue(String requestBody) throws Exception {
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
}
