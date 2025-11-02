package com.michal.openai.jira.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.JiraCreateIssueRequest;
import com.michal.openai.entity.JiraIssue;
import com.michal.openai.jira.JiraService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class JiraServiceImpl implements JiraService {

    @Value("${jira.issue}")
    private String issue;

    @Value("${jira.project.name}")
    private String projectName;

    @Value("${jira.search}")
    private String search;

    @Value("${jira.maxresults}")
    private String maxresults;

    private final int retryAttempts = 2;
    private final int waitSeconds = 5;
    private final String createIssueEndpoint = "/issue";

    @Qualifier("jiraRestClient")
    private final RestClient restClient;

    private ObjectMapper objectMapper;

    public JiraIssue getIssue(String id) {
        String getIssueEndpoint = issue + "/" + id;
        log.debug("getIssueEndpoint: {}", getIssueEndpoint);
        JiraIssue jiraIssue;
        try {
            jiraIssue = restClient.get().uri(getIssueEndpoint)
                    .retrieve()
                    .body(JiraIssue.class);
            return jiraIssue;

        }
        catch(HttpClientErrorException e)
        {
            log.error("Error getting jira issue ", e);
        }
        return null;
    }

    /*
     * Send all-issues request to Jira API, then return list of issue-objects
     */
    public List<JiraIssue> getIssues()
    {
        String urlToGet = search + "?jql=project=" + projectName;
        log.debug("getIssues() urlToGet: {}", urlToGet);

        List<JiraIssue> issues = new ArrayList<>();

        try
        {
                issues = restClient.get()
                        .uri(urlToGet)
                        .retrieve()
                        .body(new ParameterizedTypeReference<List<JiraIssue>>() {});

            return issues;
        }

        catch (Exception e) {
           log.error(e.getMessage());
        }
        return null;
    }

    /*
     * Parse requestBody of issue-data delivered by GPT, then build and send JSON-issue to Jira API
     */
    public JiraIssue createJavaIssue(String requestBody) {
    	
        String urlToCall = "/issue";
        log.info("createJavaIssue() urlToCall: {}", urlToCall);
        log.debug("requestBody : {}", requestBody);

        // Parse GPT response JSON using Jackson
        try {
            JsonNode gptAnswerJson = objectMapper.readTree(requestBody);

            String summary = gptAnswerJson.path("summary").asText("null");
            String description = gptAnswerJson.path("description").asText("null");
            String assignee = gptAnswerJson.path("assignee").asText("null");
            String dueDate = gptAnswerJson.path("dueDate").asText("null");
            String issueType = gptAnswerJson.path("issuetype").asText("Task");
            String key = gptAnswerJson.path("key").asText("JAVA");

            JiraCreateIssueRequest requestObject = new JiraCreateIssueRequest();
            JiraCreateIssueRequest.Fields fieldsObject = requestObject.new Fields(summary);
            fieldsObject.setSummary(summary);

            var desc = fieldsObject.new Description();
            desc.setType("doc");
            desc.setVersion(1);

            var content = desc.new Content();
            content.setType("paragraph");

            var contentOfContent = content.new ContentOfContent();
            contentOfContent.setText(description);
            contentOfContent.setType("text");

            content.setContentOfContent(List.of(contentOfContent));
            desc.setContent(List.of(content));
            fieldsObject.setDescription(desc);

            var issueTypeObject = fieldsObject.new Issuetype(issueType);
            fieldsObject.setIssueType(issueTypeObject);

            var project = fieldsObject.new Project();
            project.setKey(key);
            fieldsObject.setProject(project);

            requestObject.setFields(fieldsObject);

            log.debug(requestObject.toString());
            log.debug(fieldsObject.toString());
            log.debug(project.toString());
            var jiraIssueResponse = callCreateIssue(requestObject);

            return jiraIssueResponse;
            
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    private JiraIssue callCreateIssue(JiraCreateIssueRequest request) {
        for (int i = 0; i < retryAttempts; i++) {
            log.debug("Calling GPT with RestClient");
            try {
                JiraIssue jiraIssue = restClient.post()
                        .uri(createIssueEndpoint)
                        .body(request)
                        .retrieve()
                        .body(JiraIssue.class);
                return jiraIssue;

            } catch (RuntimeException e) {
                log.error("Error calling GPT! ", e);
                sleep(waitSeconds);
            }
        }
        return null;
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
