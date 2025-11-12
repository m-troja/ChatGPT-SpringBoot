package com.michal.openai.jira.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.jira.JiraService;
import com.michal.openai.jira.entity.JiraIssue;
import com.michal.openai.jira.entity.JiraIssueDto;
import com.michal.openai.jira.entity.JiraListOfIssues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.TimeUnit;

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

    /*
     * Send all-issues request to Jira API, then return list of issue-objects
     */
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

    /*
     * Parse requestBody of issue-data delivered by GPT, then build and send JSON-issue to Jira API
     */
    public JiraIssueDto createJavaIssue(String requestBody) {
    	
        String urlToCall = "/issue";
        log.info("CreateJavaIssue() urlToCall: {}", urlToCall);
        log.debug("requestBody : {}", requestBody);

        // Parse GPT response JSON using Jackson
        try {
//            JsonNode gptAnswerJson = objectMapper.readTree(requestBody);
//
//            String summary = gptAnswerJson.path("summary").asText("null");
//            String description = gptAnswerJson.path("description").asText("null");
//            String assignee = gptAnswerJson.path("assignee").asText("null");
//            String dueDate = gptAnswerJson.path("dueDate").asText("null");
//            String issueType = gptAnswerJson.path("issuetype").asText("Task");
//            String key = gptAnswerJson.path("key").asText("JAVA");
//
//            JiraIssue requestObject = new JiraIssue();
//            JiraIssue.Fields fieldsObject = new JiraIssue.Fields(summary);
//            fieldsObject.setSummary(summary);
//
//            var desc = new JiraIssue.Fields.Description();
//            desc.setType("doc");
//            desc.setVersion(1);
//
//            var content = new JiraIssue.Fields.Description.Content();
//            content.setType("paragraph");
//
//            var contentOfContent = new JiraIssue.Fields.Description.Content.ContentOfContent();
//            contentOfContent.setText(description);
//            contentOfContent.setType("text");
//
//            content.setContentOfContent(List.of(contentOfContent));
//            desc.setContent(List.of(content));
//            fieldsObject.setDescription(desc);
//
//            var issueTypeObject = new JiraIssue.Fields.Issuetype(issueType);
//            fieldsObject.setIssueType(issueTypeObject);
//
//            var project = new JiraIssue.Fields.Project();
//            project.setKey(key);
//            fieldsObject.setProject(project);
//
//            requestObject.setFields(fieldsObject);
//
//            log.debug(requestObject.toString());
//            log.debug(fieldsObject.toString());
//            log.debug(project.toString());

            JiraIssue issue = objectMapper.readValue(requestBody, JiraIssue.class);
            var jiraIssueResponse = callCreateIssue(issue);
            log.debug("Result of callCreateIssue(): {}", jiraIssueResponse);
            return jiraIssueResponse;
            
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return null;
    }

    private JiraIssueDto callCreateIssue(JiraIssue request) {
        for (int i = 0; i < retryAttempts; i++) {
            log.debug("Calling GPT with RestClient");
            try {
                return restClient.post()
                        .uri(createIssueEndpoint)
                        .body(request)
                        .retrieve()
                        .body(JiraIssueDto.class);

            } catch (RuntimeException e) {
                log.error("Error in callCreateIssue: ", e);
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
