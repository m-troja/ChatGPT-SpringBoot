package com.michal.openai.functions.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.michal.openai.jira.entity.JiraIssue;
import com.michal.openai.jira.service.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAllJiraIssuesFunctionTest {

    private JiraService jiraService;
    @Autowired ObjectMapper objectMapper;
    private GetAllJiraIssuesFunction function;

    @BeforeEach
    public void setup(){
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        jiraService = mock(JiraService.class);
        function = new GetAllJiraIssuesFunction(jiraService, objectMapper);
    }
    @Test
    public void shouldGetAllJiraIssues() throws JsonProcessingException {
        // given
        var issuesMock = buildListOfJiraIssue();
        when(jiraService.getIssues()).thenReturn(issuesMock);

        // when
        var issuesFromService = function.execute(null);

        // then
        var issuesParsedFromFunction = objectMapper.readValue(issuesFromService, new TypeReference<List<JiraIssue>>(){});
        assertThat(issuesParsedFromFunction).isEqualTo(issuesMock);

    }
    @Test
    public void shouldReturnEmptyList() {
        List<JiraIssue> issuesMock = List.of();
        when(jiraService.getIssues()).thenReturn(issuesMock);

        String emptyResponse = function.execute(null);
        assertThat(emptyResponse).isEqualTo("[]");

    }
    private List<JiraIssue> buildListOfJiraIssue() {

        var issueType = new JiraIssue.Issuetype( "JAVA");
        var contentOfContents = List.of( new JiraIssue.ContentOfContent("text", "test ContentOfContent"));
        var contents = List.of(new JiraIssue.Content(("text"), contentOfContents));
        var description = new JiraIssue.Description("text", 123, contents);
        var project = new JiraIssue.Project("JAVA");
        var status = new JiraIssue.Status("NEW");

        var jiraIssue1 =  new JiraIssue("Key", "2025-11-15", "assignee",
                new JiraIssue.Fields(
                        issueType,
                        description,
                        project,
                        "Test summary", status));
        var jiraIssue2 =  new JiraIssue("JAVA-2", "2025-11-16", "assignee1",
                new JiraIssue.Fields(
                        issueType,
                        description,
                        project,
                        "Test summary1",
                        status));
        return List.of(jiraIssue1, jiraIssue2);
    }
}
