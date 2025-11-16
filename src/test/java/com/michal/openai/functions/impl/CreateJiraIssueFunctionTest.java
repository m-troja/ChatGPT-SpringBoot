package com.michal.openai.functions.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.jira.service.JiraService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.when;

public class CreateJiraIssueFunctionTest {

    private JiraService jiraService;
    private ObjectMapper objectMapper;

    @Test
    public void shouldCreateJiraIssue() {
        // when
    }
}
