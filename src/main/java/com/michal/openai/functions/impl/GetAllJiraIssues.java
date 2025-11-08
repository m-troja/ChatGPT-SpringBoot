package com.michal.openai.functions.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.entity.JiraIssue;
import com.michal.openai.functions.Function;
import com.michal.openai.jira.JiraService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletionException;

@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GetAllJiraIssues implements Function {

    private JiraService jiraService;
    private ObjectMapper objectMapper;

    @Override
    public String execute(String arguments) {
        log.info("GetAllJiraIssues arguments: {}", arguments);
        List<JiraIssue> issues = jiraService.getIssues();
                    try {
                        String json = objectMapper.writeValueAsString(issues);
                        log.debug("Serialized {} Jira issues to JSON", issues.size());
                        return json;
                    } catch (JsonProcessingException e) {
                        log.error("Failed to serialize Jira issues", e);
                        throw new CompletionException(e);
                    }
    }
}