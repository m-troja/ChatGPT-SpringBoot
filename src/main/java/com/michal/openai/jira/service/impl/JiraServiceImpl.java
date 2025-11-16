package com.michal.openai.jira.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.exception.JiraCommunicationException;
import com.michal.openai.jira.entity.JiraCreateIssueRequest;
import com.michal.openai.jira.entity.JiraCreateIssueResponse;
import com.michal.openai.jira.entity.JiraIssue;
import com.michal.openai.jira.entity.JiraListOfIssues;
import com.michal.openai.jira.service.JiraService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@Slf4j
@Service
public class JiraServiceImpl implements JiraService {

    @Value("${jira.issue}")
    private String issueEndpoint;

    @Value("${jira.url}")
    private String jiraUrl;

    @Value("${jira.project.name}")
    private String javaProjectName;

    @Value("${jira.search}")
    private String searchEndpoint;

    @Value("${jira.max.results}")
    private String maxResults;

    @Value("${jira.fields}")
    private String fields;

    @Value("${jira.retry.attempts}")
    private int retryAttempts;

    @Value("${jira.wait.seconds}")
    private int waitSeconds;

    @Value("${jira.create.issue.endpoint}")
    private String createIssueEndpoint;

    @Qualifier("jiraRestClient")
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public JiraIssue getIssue(String id) {
        String getIssueEndpoint = issueEndpoint + "/" + id;
        log.debug("Calling getIssue to: {}{}", jiraUrl, getIssueEndpoint);
        try {
            return restClient.get()
                    .uri(getIssueEndpoint)
                    .retrieve()
                    .body(JiraIssue.class);

        }
        catch(HttpClientErrorException e)
        {
            log.error("Error getting Jira getIssue(): ", e);
        }
        return null;
    }

    public List<JiraIssue> getIssues()
    {
        String urlToGet = searchEndpoint + "?jql=project=" + javaProjectName + "&fields=" + fields +"&maxResults=" + maxResults;
        log.debug("Calling getIssues() to: {}{}", jiraUrl, urlToGet);
        JiraListOfIssues jiraListOfIssues;
        try
        {
            String rawResponse = restClient.get()
                    .uri(urlToGet)
                    .retrieve()
//                    .body(new ParameterizedTypeReference<>() {});
                    .body(String.class);
            log.debug("Raw Jira response: {}", rawResponse);

            jiraListOfIssues = objectMapper.readValue(rawResponse, JiraListOfIssues.class);

            log.debug("Result of getIssues(): {}", jiraListOfIssues);
            if (jiraListOfIssues == null || jiraListOfIssues.issues() == null) {
                return List.of();
            }
            return  jiraListOfIssues.issues();
        }
        catch (Exception e) {
           log.error("Error calling Jira getIssues(): ", e);
        }
        return List.of();
    }

    public JiraCreateIssueResponse createJavaIssue(JiraCreateIssueRequest request) {
    	
        log.debug("CreateJavaIssue() urlToCall: {}", createIssueEndpoint);
        log.debug("requestBody : {}", request);

        try {
            var jiraIssueResponse = callCreateIssue(request);
            log.debug("Result of callCreateIssue(): {}", jiraIssueResponse);
            return jiraIssueResponse;
        } catch (Exception e) {
            log.error("Error creating Jira Issue ", e);
            throw new JiraCommunicationException("Failed to create Jira issue", e);
        }
    }

    private JiraCreateIssueResponse callCreateIssue(JiraCreateIssueRequest request) {
        Exception lastException = null;

        for (int i = 0; i < retryAttempts; i++) {
            log.debug("Calling GPT with RestClient");
            try {
                var dto =  restClient.post()
                        .uri(createIssueEndpoint)
                        .body(request)
                        .retrieve()
                        .body(JiraCreateIssueResponse.class);
               log.debug("RestClient response DTO: {}" , dto);
               return dto;
            } catch (Exception e) {
                lastException = e;
                log.error("Error in callCreateIssue: ", e);
                sleep(waitSeconds);
            }
        }
        throw new JiraCommunicationException("Failed to create issue after " + retryAttempts + " attempts", lastException);
    }

    private void sleep(int seconds) {
        try {
            log.debug("Sleeping for {}s", seconds);
            TimeUnit.SECONDS.sleep(seconds);

        } catch (InterruptedException e) {
            log.error("Sleeping interrupted ", e);
            throw new RuntimeException(e);
        }
    }

    public JiraServiceImpl(@Qualifier("jiraRestClient") RestClient restClient, ObjectMapper objectMapper) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
    }
}