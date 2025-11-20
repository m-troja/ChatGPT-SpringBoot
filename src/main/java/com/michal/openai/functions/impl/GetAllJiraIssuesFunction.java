package com.michal.openai.functions.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.michal.openai.functions.Function;
import com.michal.openai.jira.service.JiraService;
import com.michal.openai.jira.entity.JiraIssue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class GetAllJiraIssuesFunction implements Function {

    private final JiraService jiraService;
    private final ObjectMapper objectMapper;

    @Override
    public String execute(String arguments) {
        log.debug("GetAllJiraIssuesFunction arguments: {}", arguments);
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