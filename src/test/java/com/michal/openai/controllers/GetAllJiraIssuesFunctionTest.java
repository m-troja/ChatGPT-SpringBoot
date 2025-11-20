package com.michal.openai.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.functions.impl.GetAllJiraIssuesFunction;
import com.michal.openai.jira.entity.JiraIssue;
import com.michal.openai.jira.service.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GetAllJiraIssuesFunctionTest {

    private JiraService jiraService;
    private ObjectMapper objectMapper;
    private GetAllJiraIssuesFunction function;

    @BeforeEach
    public void setup() {
        jiraService = mock(JiraService.class);
        objectMapper = new ObjectMapper();
        function = new GetAllJiraIssuesFunction(jiraService, objectMapper);
    }

    @Test
    public void shouldGetAllJiraIssues() throws JsonProcessingException {
        // given
        String arguments = null;
        var convertedListOfIssues = buildListOfJiraIssues();
        when(jiraService.getIssues()).thenReturn(buildListOfJiraIssues());

        // when
        String functionResponse = function.execute(arguments);

        // then
        assertThat(functionResponse).isEqualTo(objectMapper.writeValueAsString(convertedListOfIssues));
    }

    private String buildJiraResponseOf3Issues() {
        return """
                {"issues":[
                {"expand":"renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations","id":"10300","self":"https://mtroja98.atlassian.net/rest/api/3/issue/10300","key":"JAVA-93","fields":{"summary":"Test summary2","issuetype":{"self":"https://mtroja98.atlassian.net/rest/api/3/issuetype/10001","id":"10001","description":"Tasks track small, distinct pieces of work.","iconUrl":"https://mtroja98.atlassian.net/rest/api/2/universal_avatar/view/type/issuetype/avatar/10318?size=medium","name":"Task","subtask":false,"avatarId":10318,"entityId":"2f9dddd5-938a-45f6-8584-807bfdd9fbcf","hierarchyLevel":0},"description":{"type":"doc","version":1,"content":[{"type":"paragraph","content":[{"type":"text","text":"test ContentOfContent req"}]}]},"assignee":null,"status":{"self":"https://mtroja98.atlassian.net/rest/api/3/status/10000","description":"","iconUrl":"https://mtroja98.atlassian.net/","name":"To Do","id":"10000","statusCategory":{"self":"https://mtroja98.atlassian.net/rest/api/3/statuscategory/2","id":2,"key":"new","colorName":"blue-gray","name":"To Do"}}}},
                {"expand":"renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations","id":"10299","self":"https://mtroja98.atlassian.net/rest/api/3/issue/10299","key":"JAVA-92","fields":{"summary":"Test summary2","issuetype":{"self":"https://mtroja98.atlassian.net/rest/api/3/issuetype/10001","id":"10001","description":"Tasks track small, distinct pieces of work.","iconUrl":"https://mtroja98.atlassian.net/rest/api/2/universal_avatar/view/type/issuetype/avatar/10318?size=medium","name":"Task","subtask":false,"avatarId":10318,"entityId":"2f9dddd5-938a-45f6-8584-807bfdd9fbcf","hierarchyLevel":0},"description":{"type":"doc","version":1,"content":[{"type":"paragraph","content":[{"type":"text","text":"test ContentOfContent req"}]}]},"assignee":null,"status":{"self":"https://mtroja98.atlassian.net/rest/api/3/status/10000","description":"","iconUrl":"https://mtroja98.atlassian.net/","name":"To Do","id":"10000","statusCategory":{"self":"https://mtroja98.atlassian.net/rest/api/3/statuscategory/2","id":2,"key":"new","colorName":"blue-gray","name":"To Do"}}}},
                {"expand":"renderedFields,names,schema,operations,editmeta,changelog,versionedRepresentations","id":"10297","self":"https://mtroja98.atlassian.net/rest/api/3/issue/10297","key":"JAVA-90","fields":{"summary":"Test summary2","issuetype":{"self":"https://mtroja98.atlassian.net/rest/api/3/issuetype/10001","id":"10001","description":"Tasks track small, distinct pieces of work.","iconUrl":"https://mtroja98.atlassian.net/rest/api/2/universal_avatar/view/type/issuetype/avatar/10318?size=medium","name":"Task","subtask":false,"avatarId":10318,"entityId":"2f9dddd5-938a-45f6-8584-807bfdd9fbcf","hierarchyLevel":0},"description":{"type":"doc","version":1,"content":[{"type":"paragraph","content":[{"type":"text","text":"test ContentOfContent req"}]}]},"assignee":null,"status":{"self":"https://mtroja98.atlassian.net/rest/api/3/status/10000","description":"","iconUrl":"https://mtroja98.atlassian.net/","name":"To Do","id":"10000","statusCategory":{"self":"https://mtroja98.atlassian.net/rest/api/3/statuscategory/2","id":2,"key":"new","colorName":"blue-gray","name":"To Do"}}}}],
                "nextPageToken":"ChkjU3RyaW5nJlNrRldRUT09JUludCZPVEE9EAMYg_2Z9qgzIgxwcm9qZWN0PUpBVkEqAltd","isLast":false}
                """;
    }

    private List<JiraIssue> buildListOfJiraIssues() {
        var issueType = new JiraIssue.Issuetype( "Task");
        var contentOfContents = List.of( new JiraIssue.ContentOfContent("text", "Tasks track small, distinct pieces of work"));
        var contents = List.of(new JiraIssue.Content(("paragraph"), contentOfContents));
        var description = new JiraIssue.Description("doc", 1, contents);
        var project = new JiraIssue.Project("JAVA");
        var status = new JiraIssue.Status("NEW");
        var fields = new JiraIssue.Fields(issueType, description, project,"Test summary2", status);
        var issue1 = new JiraIssue("JAVA-93", null, null,fields);
        var issue2 = new JiraIssue("JAVA-92", null, null,fields);
        var issue3 = new JiraIssue("JAVA-90", null, null,fields);
        return List.of(issue1, issue2, issue3);
    }
}
