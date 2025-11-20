package com.michal.openai.jira.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JiraIssueDto(
        String key,
        String summary ,
        String description,
        String duedate,
        String assignee,
        @JsonProperty("issuetype") String issueType
) {}
