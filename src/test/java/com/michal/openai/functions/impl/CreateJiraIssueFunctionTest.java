package com.michal.openai.functions.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.jira.JiraCnv;
import com.michal.openai.jira.entity.JiraCreateIssueRequest;
import com.michal.openai.jira.entity.JiraCreateIssueResponse;
import com.michal.openai.jira.service.JiraService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

class CreateJiraIssueFunctionTest {

    private JiraService jiraService;
    private JiraCnv jiraCnv;
    private CreateJiraIssueFunction function;

    @BeforeEach
    public void setup() {
        jiraService = mock(JiraService.class);
        ObjectMapper objectMapper = new ObjectMapper();
        jiraCnv = mock(JiraCnv.class);
        function = new CreateJiraIssueFunction(jiraService, objectMapper, jiraCnv);
    }

    @Test
    public void shouldCreateJiraIssue() {
        String requestBody = buildRequestBody();
        // given
        when(jiraCnv.convertFunctionRequestBodyToJiraCreateIssueRequest(requestBody)).thenReturn(buildCreateIssueRequest());

        var response = new JiraCreateIssueResponse("10000", "JAVA-1", "http://test");
        when(jiraService.createJavaIssue(buildCreateIssueRequest())).thenReturn(response);

        // when
        String functionExecution = function.execute(requestBody);

        // then
        assertThat(functionExecution).contains("JAVA-1");
        assertThat(functionExecution).contains("10000");
        verify(jiraCnv, times(1)).convertFunctionRequestBodyToJiraCreateIssueRequest(requestBody);
        verify(jiraService, times(1)).createJavaIssue(buildCreateIssueRequest());

    }

    private String buildRequestBody() {
        return """
                {
                  "fields": {
                    "issuetype": {
                      "name": "Task"
                    },
                    "description": {
                      "type": "doc",
                      "version": 1,
                      "content": [
                        {
                          "type": "paragraph",
                          "content": [
                            {
                              "type": "text",
                              "text": "Test desc"
                            }
                          ]
                        }
                      ]
                    },
                    "project": {
                      "key": "JAVA"
                    },
                    "summary": "Test summary2"
                  }
                }""";
    }

    private JiraCreateIssueRequest buildCreateIssueRequest() {
        var issueType = new JiraCreateIssueRequest.Issuetype( "Task");
        var contentOfContents = List.of( new JiraCreateIssueRequest.ContentOfContent("text", "Test desc"));
        var contents = List.of(new JiraCreateIssueRequest.Content(("paragraph"), contentOfContents));
        var description = new JiraCreateIssueRequest.Description("doc", 1, contents);
        var project = new JiraCreateIssueRequest.Project("JAVA");
        return new JiraCreateIssueRequest(
                new JiraCreateIssueRequest.Fields(
                        issueType,
                        description,
                        project,
                        "Test summary2"));
    }
}
