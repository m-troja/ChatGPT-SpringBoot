package com.michal.openai.jira.entity;

public record JiraFunctionRequestBody(
        String issueType,
        String description,
        String summary,
        String project
) {
}
