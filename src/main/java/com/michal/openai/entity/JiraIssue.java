package com.michal.openai.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public record JiraIssue(
        String key,
        String summary ,
        String description,
        String duedate,
        String assignee,
        @JsonProperty("issuetype") String issueType
) {}
