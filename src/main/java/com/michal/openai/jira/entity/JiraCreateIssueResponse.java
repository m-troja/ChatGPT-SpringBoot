package com.michal.openai.jira.entity;

public record JiraCreateIssueResponse(
        String id,
        String key,
        String self
) {}