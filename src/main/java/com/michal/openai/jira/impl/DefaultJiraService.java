package com.michal.openai.jira.impl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.JiraCreateIssueRequest;
import com.michal.openai.entity.JiraIssue;
import com.michal.openai.jira.JiraService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Async
public class DefaultJiraService implements JiraService {

    @Value("${jira.url}")
    private String url;

    @Value("${jira.key}")
    private String jiraKey;

    @Value("${jira.issue}")
    private String issue;

    @Value("${jira.project.name}")
    private String projectName;

    @Value("${jira.search}")
    private String search;

    @Value("${jira.maxresults}")
    private String maxresults;

    @Autowired
    HttpClient httpClient;

    @Autowired
    ObjectMapper objectMapper;

    public String getIssueJson(String id) {
        String urlToGet = url + issue + "/" + id;
        log.info("getIssueJson urlToGet: " + urlToGet);

        HttpGet httpGet = new HttpGet(urlToGet);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + jiraKey);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        log.debug("httpGet: " + httpGet.toString());

        try {
            HttpResponse httpResponse = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8);

            log.debug("responseBody: " + responseBody);

            return responseBody;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * Send request of all issue to Jira API, then return list of issue-objects
     */
    public CompletableFuture<List<JiraIssue>> getIssues() {
        String urlToGet = url + search + "?jql=project=" + projectName;
        log.info("getIssues() urlToGet: " + urlToGet);

        HttpGet httpGet = new HttpGet(urlToGet);
        httpGet.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + jiraKey);
        httpGet.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        httpGet.setHeader(HttpHeaders.ACCEPT, "application/json");

        log.debug("httpGet: " + httpGet.toString());

        List<JiraIssue> issues = new ArrayList<>();

        try {
            HttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

            log.info("responseBody: " + responseBody);

            // Use Jackson to parse the response
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // Extract 'issues' array
            JsonNode issuesNode = rootNode.path("issues");

            // Process each issue
            for (JsonNode issueNode : issuesNode) {
                JiraIssue jiraIssue = new JiraIssue();
                jiraIssue.setKey(issueNode.path("key").asText());

                JsonNode fieldsNode = issueNode.path("fields");

                if (fieldsNode.has("summary")) {
                    jiraIssue.setSummary(fieldsNode.path("summary").asText());
                }

                // Extract issue type description
                if (fieldsNode.has("issuetype")) {
                    JsonNode issuetypeNode = fieldsNode.path("issuetype");
                    if (issuetypeNode.has("description")) {
                        jiraIssue.setDescription(issuetypeNode.path("description").asText());
                    }
                }

                // Extract assignee details
                if (fieldsNode.has("assignee")) {
                    JsonNode assigneeNode = fieldsNode.path("assignee");
                    if (assigneeNode.has("displayName")) {
                        jiraIssue.setAssignee(assigneeNode.path("displayName").asText());
                    }
                }

                // Extract due date
                if (fieldsNode.has("duedate")) {
                    jiraIssue.setDuedate(fieldsNode.path("duedate").asText());
                }

                issues.add(jiraIssue);
                log.debug("jiraIssue: " + jiraIssue.toString());
            }
            return CompletableFuture.completedFuture(issues);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /*
     * Parse requestBody of issue-data delivered by GPT, then build and send JSON-issue to Jira API
     */
    public CompletableFuture<String> createJavaIssue(String requestBody) {
    	
        String urlToCall = url + "/issue";
        log.info("createJavaIssue() urlToCall: " + urlToCall);
        log.debug("requestBody : " + requestBody);

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

            log.info("urlToCall = " + urlToCall);
            StringEntity stringEntity = new StringEntity(objectMapper.writeValueAsString(requestObject), "UTF-8");
            HttpPost httpPost = new HttpPost(urlToCall);
            httpPost.setEntity(stringEntity);
            httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
            httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + jiraKey);

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();

            log.debug("httpEntity : " + httpEntity.toString());

            String responseBody = EntityUtils.toString(httpEntity);

            log.debug("RESPONSE : " + responseBody);

            JsonNode responseObject = objectMapper.readTree(responseBody);
            String returnedKey = "ticketID:" + responseObject.path("key").asText();
            requestObject.setReturnedKey(returnedKey);

            log.debug("requestObject : " + objectMapper.writeValueAsString(requestObject));

            return CompletableFuture.completedFuture(objectMapper.writeValueAsString(requestObject));
            
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
