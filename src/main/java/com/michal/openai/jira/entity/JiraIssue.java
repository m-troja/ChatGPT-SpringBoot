package com.michal.openai.jira.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JiraIssue(
        String key,
        String dueDate,
        String assignee,
        Fields fields
        )
{
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record  Fields(
            @JsonProperty("issuetype") Issuetype issueType,
            Description description,
            Project project,
            String summary,
            Status status
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Project( String key) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Issuetype(String name) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Status (String name){}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Description( String type, Integer version, List<Content> content){}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Content(String type, @JsonProperty("content")  List<ContentOfContent> contentOfContent){}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentOfContent(String type, String text) {}
}
