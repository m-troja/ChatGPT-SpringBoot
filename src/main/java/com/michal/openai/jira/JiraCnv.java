package com.michal.openai.jira;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.jira.entity.JiraCreateIssueRequest;
import com.michal.openai.jira.entity.JiraFunctionRequestBody;
import com.michal.openai.jira.entity.JiraIssue;
import com.michal.openai.jira.entity.JiraIssueDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraCnv {

    private final ObjectMapper objectMapper;

    public JiraCreateIssueRequest convertFunctionRequestBodyToJiraCreateIssueRequest(String requestBody) {

        JiraFunctionRequestBody request;
        try {
            request = objectMapper.readValue(requestBody, JiraFunctionRequestBody.class);
        } catch (JsonProcessingException e) {
            log.error("Error converting FunctionRequestBody String", e);
            throw new RuntimeException(e);
        }
        var issueType = new JiraCreateIssueRequest.Issuetype( request.issueType() );
        var contentOfContents = List.of( new JiraCreateIssueRequest.ContentOfContent("text", "test ContentOfContent req"));
        var contents = List.of(new JiraCreateIssueRequest.Content(("paragraph"), contentOfContents));
        var description = new JiraCreateIssueRequest.Description("doc", 1, contents);
        var project = new JiraCreateIssueRequest.Project("JAVA");
        var convertedRequest =  new JiraCreateIssueRequest(
                new JiraCreateIssueRequest.Fields(
                        issueType,
                        description,
                        project,
                        "Test summary2"));
        log.debug("Converted body:");
        log.debug(requestBody);
        log.debug("To intermediate JiraFunctionRequestBody:");
        log.debug("{}", request);
        log.debug("Final object:");
        log.debug("{}",convertedRequest);
        log.debug("Final object to JSON:");
        try {
            log.debug("{}",objectMapper.writeValueAsString(convertedRequest));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return convertedRequest;

    }
    public JiraIssueDto convertIssueToIssueDto(JiraIssue jiraIssue) {
        var dto = new JiraIssueDto(
                jiraIssue.key(),
                jiraIssue.fields().summary(),
                jiraIssue.fields().description().content().getFirst().contentOfContent().getFirst().text(),
                jiraIssue.dueDate(),
                jiraIssue.assignee(),
                jiraIssue.fields().issueType().name()
        );
        log.debug("Converted {} to {}", jiraIssue, dto);
        return dto;
    }

    public List<JiraIssueDto> convertListOfIssuesToListOfIssueDto(List<JiraIssue> issues) {
        return issues.stream().map(this::convertIssueToIssueDto).toList();
    }
}
